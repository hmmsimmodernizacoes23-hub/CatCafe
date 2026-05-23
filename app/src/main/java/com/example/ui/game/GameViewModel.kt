package com.example.ui.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.SaveState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class CafeItem(val displayName: String, val emoji: String, val basePrice: Int, val basePrepTimeMs: Long) {
    COFFEE("Deluxe Coffee", "☕", 15, 3000L),
    MATCHA("Sweet Matcha", "🍵", 20, 5000L),
    CAKE("Cat Cake Slice", "🍰", 35, 8000L),
    TOY_FISH("Fish Nibble", "🐟", 10, 2000L),
    TOY_YARN("Yarn Ball", "🧶", 12, 4000L)
}

enum class GameScreen {
    MENU,
    PLAYING,
    SHIFT_SUMMARY,
    SHOP,
    GAMEOVER
}

data class CatModel(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val description: String,
    val cost: Int,
    val tableId: Int
)

data class CustomerModel(
    val id: Int,
    val name: String,
    val orderedItem: CafeItem,
    var patience: Float, // Current patience in seconds
    val maxPatience: Float, // Max patience in seconds
    var isServed: Boolean = false,
    val avatarEmoji: String = "👤"
)

data class TableState(
    val id: Int, // Table / Table ID 1-4
    val isUnlocked: Boolean,
    val cat: CatModel,
    val customer: CustomerModel? = null,
    val isPettingFeedbackActive: Boolean = false
)

data class PrepStationState(
    val id: String, // "coffee", "matcha", "cake", "toy_fish", "toy_yarn"
    val itemType: CafeItem,
    val isBrewing: Boolean = false,
    val progress: Float = 0f,
    val durationMs: Long = 0L,
    val elapsedMs: Long = 0L
)

data class FloatingParticle(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val color: Color,
    val label: String,
    val x: Float, // Percentage / position
    val y: Float,
    val scale: Float = 1f,
    var ageTicks: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    
    // UI Game screen navigation
    private val _screenState = MutableStateFlow(GameScreen.MENU)
    val screenState: StateFlow<GameScreen> = _screenState.asStateFlow()

    // Persistent attributes loaded from Room
    private val _saveState = MutableStateFlow(SaveState())
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // Transient Gameplay Attributes
    private val _coinsEarnedToday = MutableStateFlow(0)
    val coinsEarnedToday: StateFlow<Int> = _coinsEarnedToday.asStateFlow()

    private val _loveEarnedToday = MutableStateFlow(0)
    val loveEarnedToday: StateFlow<Int> = _loveEarnedToday.asStateFlow()

    private val _lives = MutableStateFlow(5) // Max 5 hearts
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _shiftTimeLeft = MutableStateFlow(80.0f) // Day Shift duration in seconds
    val shiftTimeLeft: StateFlow<Float> = _shiftTimeLeft.asStateFlow()

    // Active tables and Cats State
    private val _tables = MutableStateFlow<List<TableState>>(emptyList())
    val tables: StateFlow<List<TableState>> = _tables.asStateFlow()

    // Tray serving counter: Holds up to 4 finished items
    private val _traySlots = MutableStateFlow<List<CafeItem?>>(listOf(null, null, null, null))
    val traySlots: StateFlow<List<CafeItem?>> = _traySlots.asStateFlow()

    // Active Preparation stations state
    private val _brewStations = MutableStateFlow<List<PrepStationState>>(emptyList())
    val brewStations: StateFlow<List<PrepStationState>> = _brewStations.asStateFlow()

    // Particles animations array
    private val _particles = MutableStateFlow<List<FloatingParticle>>(emptyList())
    val particles: StateFlow<List<FloatingParticle>> = _particles.asStateFlow()

    // Drag-and-drop state info
    private val _draggedItemIndex = MutableStateFlow<Int?>(null)
    val draggedItemIndex: StateFlow<Int?> = _draggedItemIndex.asStateFlow()

    private val _dragOffset = MutableStateFlow(Offset.Zero)
    val dragOffset: StateFlow<Offset> = _dragOffset.asStateFlow()

    private var gameLoopJob: Job? = null
    private var customerIdCounter = 1

    private val catAssets = listOf(
        CatModel("Ginger", "Ginger", "🐱", Color(0xFFE67E22), "Orange tabby cat. Super cuddle buddy!", 0, 1),
        CatModel("Boba", "Boba", "🐈", Color(0xFFD35400), "Siamese cat. Highly playful & sweet.", 0, 2),
        CatModel("Mochi", "Mochi", "😻", Color(0xFFF39C12), "Calico cat. Soft biscuit maker.", 200, 3),
        CatModel("Shadow", "Shadow", "🐈‍⬛", Color(0xFF2C3E50), "Black cat. Silent sleeper.", 500, 4)
    )

    private val customerNames = listOf(
        "Clara", "Leo", "Sienna", "Felix", "Melody", "Oliver", "Maya", "Jasper", "Chloe", "Teddy",
        "Daisy", "Ziggy", "Winston", "Bella", "Luna", "Toby", "Gizmo", "Sasha", "Max", "Lola"
    )

    private val customerAvatars = listOf("🧑‍🦰", "👩‍🦱", "🧔", "👩‍⚕️", "👨‍🍳", "👨‍🎓", "👩‍💼", "👱‍♀️", "👨‍🎨", "👩‍🌾")

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.saveStateDao())

        // Pull persistence flows
        viewModelScope.launch {
            repository.saveStateFlow.collectLatest { progress ->
                if (progress != null) {
                    _saveState.value = progress
                    initializeTables(progress)
                } else {
                    // Create defaults if Database is empty
                    repository.saveProgress(SaveState())
                }
            }
        }

        resetPrepStations()
    }

    private fun initializeTables(saveState: SaveState) {
        val mapped = catAssets.map { cat ->
            TableState(
                id = cat.tableId,
                isUnlocked = cat.tableId <= saveState.unlockedTables,
                cat = cat,
                customer = null
            )
        }
        _tables.value = mapped
    }

    private fun resetPrepStations() {
        _brewStations.value = listOf(
            PrepStationState("coffee", CafeItem.COFFEE),
            PrepStationState("matcha", CafeItem.MATCHA),
            PrepStationState("cake", CafeItem.CAKE),
            PrepStationState("toy_fish", CafeItem.TOY_FISH),
            PrepStationState("toy_yarn", CafeItem.TOY_YARN)
        )
    }

    // Nav actions
    fun navigateTo(screen: GameScreen) {
        _screenState.value = screen
        if (screen == GameScreen.PLAYING) {
            startGamePlay()
        } else {
            stopGamePlay()
        }
    }

    private fun startGamePlay() {
        stopGamePlay()
        _coinsEarnedToday.value = 0
        _loveEarnedToday.value = 0
        _lives.value = 5
        _shiftTimeLeft.value = 85.0f
        _traySlots.value = listOf(null, null, null, null)
        _particles.value = emptyList()
        resetPrepStations()
        
        // Ensure customer empty on starting
        initializeTables(_saveState.value)

        customerIdCounter = 1

        gameLoopJob = viewModelScope.launch {
            var lastSpawnTicks = 0
            while (_lives.value > 0 && _shiftTimeLeft.value > 0.0f) {
                delay(100) // Ticks of 100ms
                
                // 1. Tick Day Clock
                _shiftTimeLeft.value = (_shiftTimeLeft.value - 0.1f).coerceAtLeast(0.0f)

                // 2. Tick Customer Patience & Serving transitions
                tickCustomers()

                // 3. Tick Brewing stations progression
                tickBrewStations()

                // 4. Tick animations/particles
                tickParticles()

                // 5. Spawn new customers
                lastSpawnTicks++
                if (lastSpawnTicks >= 45) { // every 4.5 seconds try to spawn
                    lastSpawnTicks = 0
                    trySpawnCustomer()
                }
            }

            // End of Day logic
            if (_lives.value <= 0) {
                _screenState.value = GameScreen.GAMEOVER
            } else {
                completeShift()
            }
        }
    }

    private fun stopGamePlay() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun tickCustomers() {
        _tables.value = _tables.value.map { table ->
            if (!table.isUnlocked || table.customer == null) {
                table
            } else {
                val cust = table.customer
                if (cust.isServed) {
                    table
                } else {
                    // Pet speed dampener: If table cat is pet feedback active, customer loses patience 30% slower
                    val decor = getPlacedDecorForTable(table.id, _saveState.value)
                    val happiness = 100 + (decor?.happinessBonus ?: 0)
                    val decorPatienceDampener = if (happiness > 100) {
                        (1.0f - ((happiness - 100) / 180f)).coerceIn(0.40f, 1.0f)
                    } else {
                        1.0f
                    }
                    val patienceDrainMultiplier = (if (table.isPettingFeedbackActive) 0.6f else 1.0f) * decorPatienceDampener
                    val basePatience = cust.patience - (0.1f * patienceDrainMultiplier)
                    
                    if (basePatience <= 0) {
                        // Lost customer!
                        _lives.value = (_lives.value - 1).coerceAtLeast(0)
                        addParticle(
                            emoji = "💢",
                            color = Color.Red,
                            label = "Oh no! Patience ran out!",
                            x = (table.id * 22f),
                            y = 35f
                        )
                        table.copy(customer = null)
                    } else {
                        table.copy(customer = cust.copy(patience = basePatience))
                    }
                }
            }
        }
    }

    private fun tickBrewStations() {
        val currentSave = _saveState.value
        _brewStations.value = _brewStations.value.map { station ->
            if (!station.isBrewing) {
                station
            } else {
                val currentSpeedLevel = when (station.itemType) {
                    CafeItem.COFFEE, CafeItem.MATCHA -> currentSave.upgradeCoffeeOvenLevel
                    CafeItem.CAKE -> currentSave.upgradePastryOvenLevel
                    CafeItem.TOY_FISH, CafeItem.TOY_YARN -> currentSave.upgradeCatToysLevel
                }
                
                // Cut time by 15% per speed level upgraded
                val speedupRatio = 1.0f - ((currentSpeedLevel - 1) * 0.15f).coerceAtMost(0.6f)
                val targetDuration = (station.durationMs * speedupRatio).toLong()
                
                val nextElapsed = station.elapsedMs + 100
                if (nextElapsed >= targetDuration) {
                    // Harvest! Put to tray
                    val wasAdded = addItemToTray(station.itemType)
                    if (wasAdded) {
                        addParticle(
                            emoji = station.itemType.emoji,
                            color = Color(0xFF2ECC71),
                            label = "${station.itemType.displayName} Ready!",
                            x = 50f,
                            y = 80f
                        )
                        station.copy(isBrewing = false, progress = 0f, elapsedMs = 0L)
                    } else {
                        // Tray full blocking state - progress stuck at 100%
                        station.copy(progress = 1.0f, elapsedMs = targetDuration)
                    }
                } else {
                    station.copy(
                        progress = nextElapsed.toFloat() / targetDuration,
                        elapsedMs = nextElapsed
                    )
                }
            }
        }
    }

    private fun addItemToTray(item: CafeItem): Boolean {
        val currentTray = _traySlots.value.toMutableList()
        val firstEmptyIndex = currentTray.indexOf(null)
        if (firstEmptyIndex != -1) {
            currentTray[firstEmptyIndex] = item
            _traySlots.value = currentTray
            return true
        }
        return false
    }

    private fun trySpawnCustomer() {
        // Find empty unlocked tables
        val unlockedFreeTables = _tables.value.filter { it.isUnlocked && it.customer == null }
        if (unlockedFreeTables.isEmpty()) return

        // Spawn probability increases on higher levels
        val spawnChanceMultiplier = 0.5f + (0.05f * _saveState.value.currentDay)
        if (Random.nextFloat() > spawnChanceMultiplier) return

        // Select a table at random
        val targetTable = unlockedFreeTables.random()
        
        // Random customer options
        val name = customerNames.random()
        val avatar = customerAvatars.random()
        
        // Menu unlocks according to Day
        val recipesAvailable = mutableListOf(CafeItem.COFFEE, CafeItem.TOY_FISH)
        if (_saveState.value.currentDay >= 2) recipesAvailable.add(CafeItem.TOY_YARN)
        if (_saveState.value.currentDay >= 3) recipesAvailable.add(CafeItem.MATCHA)
        if (_saveState.value.currentDay >= 4) recipesAvailable.add(CafeItem.CAKE)

        val ordered = recipesAvailable.random()
        
        // Max customer patience
        val cushion = 8.0f * (_saveState.value.upgradeCatToysLevel - 1).coerceAtMost(3)
        val maxPat = (28f + Random.nextInt(12) + cushion).coerceIn(20f, 60f)

        val newCustomer = CustomerModel(
            id = customerIdCounter++,
            name = name,
            orderedItem = ordered,
            patience = maxPat,
            maxPatience = maxPat,
            avatarEmoji = avatar
        )

        _tables.value = _tables.value.map { table ->
            if (table.id == targetTable.id) {
                table.copy(customer = newCustomer)
            } else {
                table
            }
        }

        addParticle(
            emoji = "🐾",
            color = Color(0xFFF39C12),
            label = "$name walked in!",
            x = (targetTable.id * 22f),
            y = 40f
        )
    }

    fun triggerPrep(stationId: String) {
        _brewStations.value = _brewStations.value.map { station ->
            if (station.id == stationId && !station.isBrewing) {
                station.copy(
                    isBrewing = true,
                    progress = 0f,
                    elapsedMs = 0L,
                    durationMs = station.itemType.basePrepTimeMs
                )
            } else {
                station
            }
        }
    }

    // Drag-and-drop handles
    fun startDragging(index: Int) {
        if (_traySlots.value[index] != null) {
            _draggedItemIndex.value = index
            _dragOffset.value = Offset.Zero
        }
    }

    fun onDrag(offset: Offset) {
        _dragOffset.value = _dragOffset.value + offset
    }

    fun stopDraggingAndServe(destinationTableId: Int?) {
        val draggedIdx = _draggedItemIndex.value
        _draggedItemIndex.value = null
        _dragOffset.value = Offset.Zero

        if (draggedIdx == null) return
        val itemServed = _traySlots.value[draggedIdx] ?: return

        if (destinationTableId == null) return

        // Attempt serving
        var servedSuccess = false
        _tables.value = _tables.value.map { table ->
            if (table.id == destinationTableId && table.isUnlocked && table.customer != null) {
                val cust = table.customer
                if (cust.orderedItem == itemServed && !cust.isServed) {
                    servedSuccess = true
                    // Award score and coins immediately
                    val rateMultiplier = 1.0f + ((_saveState.value.upgradeCoffeeOvenLevel - 1) * 0.15f)
                    val payout = (itemServed.basePrice * rateMultiplier).toInt()
                    _coinsEarnedToday.value += payout
                    _loveEarnedToday.value += 3
                    
                    addParticle(
                        emoji = "🪙",
                        color = Color(0xFFF1C40F),
                        label = "+$payout Coins!",
                        x = (table.id * 22f),
                        y = 30f
                    )
                    
                    addParticle(
                        emoji = "💖",
                        color = Color.Red,
                        label = "+3 Lover!",
                        x = (table.id * 22f) + 5f,
                        y = 25f
                    )

                    // Clear customer
                    table.copy(customer = null)
                } else {
                    table
                }
            } else {
                table
            }
        }

        if (servedSuccess) {
            // Remove from tray
            val updatedTray = _traySlots.value.toMutableList()
            updatedTray[draggedIdx] = null
            _traySlots.value = updatedTray
        } else {
            // Mismatch message
            addParticle(
                emoji = "❌",
                color = Color.Gray,
                label = "Mismatched Order!",
                x = 50f,
                y = 50f
            )
        }
    }

    fun discardTrayItem(index: Int) {
        val currentTray = _traySlots.value.toMutableList()
        val discarded = currentTray[index]
        if (discarded != null) {
            currentTray[index] = null
            _traySlots.value = currentTray
            addParticle(
                emoji = "🗑️",
                color = Color.LightGray,
                label = "Discarded ${discarded.displayName}",
                x = 50f,
                y = 85f
            )
        }
    }

    // Cat Petting interaction
    fun petCat(tableId: Int) {
        _tables.value = _tables.value.map { table ->
            if (table.id == tableId && table.isUnlocked) {
                // Perform Petting Action
                val updatedLove = _loveEarnedToday.value + 1
                _loveEarnedToday.value = updatedLove
                
                // Patience bonus for petting
                val updatedCustomer = table.customer?.let { customer ->
                    val addedPatience = (customer.patience + 6.0f).coerceAtMost(customer.maxPatience)
                    customer.copy(patience = addedPatience)
                }

                // Hearts effect
                addParticle(
                    emoji = "💕",
                    color = Color.Magenta,
                    label = "Purr!! Cat happy!",
                    x = (tableId * 22f),
                    y = 35f
                )

                // Trigger brief Visual petting feedback flag
                viewModelScope.launch {
                    delay(800)
                    removePettingFeedback(tableId)
                }

                table.copy(
                    customer = updatedCustomer,
                    isPettingFeedbackActive = true
                )
            } else {
                table
            }
        }
    }

    private fun removePettingFeedback(tableId: Int) {
        _tables.value = _tables.value.map { table ->
            if (table.id == tableId) {
                table.copy(isPettingFeedbackActive = false)
            } else {
                table
            }
        }
    }

    // Save/Update progression in Room Database
    private fun completeShift() {
        stopGamePlay()
        viewModelScope.launch {
            val currentSave = _saveState.value
            val totalCoins = currentSave.coins + _coinsEarnedToday.value
            val totalLove = currentSave.catLove + _loveEarnedToday.value
            
            val newHighScore = if (_coinsEarnedToday.value > currentSave.highScore) {
                _coinsEarnedToday.value
            } else {
                currentSave.highScore
            }

            val nextProgress = currentSave.copy(
                coins = totalCoins,
                catLove = totalLove,
                highScore = newHighScore
            )
            repository.saveProgress(nextProgress)
            _screenState.value = GameScreen.SHIFT_SUMMARY
        }
    }

    fun startNextDay() {
        viewModelScope.launch {
            val currentSave = _saveState.value
            val updatedProgress = currentSave.copy(currentDay = currentSave.currentDay + 1)
            repository.saveProgress(updatedProgress)
            _screenState.value = GameScreen.PLAYING
            startGamePlay()
        }
    }

    fun resetGameProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            _screenState.value = GameScreen.MENU
        }
    }

    // Shop upgrades
    fun buyUpgrade(type: String) {
        viewModelScope.launch {
            val currentSave = _saveState.value
            var coins = currentSave.coins
            var updatedSave = currentSave

            when (type) {
                "table_3" -> {
                    if (coins >= 200 && currentSave.unlockedTables < 3) {
                        coins -= 200
                        updatedSave = currentSave.copy(
                            coins = coins,
                            unlockedTables = 3,
                            unlockedCatsCsv = currentSave.unlockedCatsCsv + ",Mochi"
                        )
                    }
                }
                "table_4" -> {
                    if (coins >= 500 && currentSave.unlockedTables < 4) {
                        coins -= 500
                        updatedSave = currentSave.copy(
                            coins = coins,
                            unlockedTables = 4,
                            unlockedCatsCsv = currentSave.unlockedCatsCsv + ",Shadow"
                        )
                    }
                }
                "coffee_speed" -> {
                    val cost = currentSave.upgradeCoffeeOvenLevel * 100
                    if (coins >= cost) {
                        coins -= cost
                        updatedSave = currentSave.copy(
                            coins = coins,
                            upgradeCoffeeOvenLevel = currentSave.upgradeCoffeeOvenLevel + 1
                        )
                    }
                }
                "pastry_speed" -> {
                    val cost = currentSave.upgradePastryOvenLevel * 120
                    if (coins >= cost) {
                        coins -= cost
                        updatedSave = currentSave.copy(
                            coins = coins,
                            upgradePastryOvenLevel = currentSave.upgradePastryOvenLevel + 1
                        )
                    }
                }
                "toys_patience" -> {
                    val cost = currentSave.upgradeCatToysLevel * 80
                    if (coins >= cost) {
                        coins -= cost
                        updatedSave = currentSave.copy(
                            coins = coins,
                            upgradeCatToysLevel = currentSave.upgradeCatToysLevel + 1
                        )
                    }
                }
            }

            repository.saveProgress(updatedSave)
        }
    }

    fun getPlacedDecorForTable(tableId: Int, saveState: SaveState): DecorType? {
        if (saveState.placedDecorsCsv.isEmpty()) return null
        val mappings = saveState.placedDecorsCsv.split(",")
        for (mapping in mappings) {
            val parts = mapping.split(":")
            if (parts.size == 2) {
                val tId = parts[0].toIntOrNull()
                if (tId == tableId) {
                    return DecorType.fromKey(parts[1])
                }
            }
        }
        return null
    }

    fun getPurchasedDecors(saveState: SaveState): List<DecorType> {
        if (saveState.purchasedDecorsCsv.isEmpty()) return emptyList()
        return saveState.purchasedDecorsCsv.split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { DecorType.fromKey(it) }
    }

    fun buyDecor(decor: DecorType) {
        viewModelScope.launch {
            val currentSave = _saveState.value
            val coins = currentSave.coins
            if (coins >= decor.cost) {
                val list = getPurchasedDecors(currentSave).toMutableList()
                if (!list.contains(decor)) {
                    list.add(decor)
                    val newCoins = coins - decor.cost
                    val newPurchasedCsv = list.joinToString(",") { it.key }
                    val updatedSave = currentSave.copy(
                        coins = newCoins,
                        purchasedDecorsCsv = newPurchasedCsv
                    )
                    repository.saveProgress(updatedSave)
                    
                    addParticle(
                        emoji = decor.emoji,
                        color = Color(0xFFF1C40F),
                        label = "Bought ${decor.displayName}!",
                        x = 50f,
                        y = 60f
                    )
                }
            }
        }
    }

    fun placeDecor(tableId: Int, decorKey: String?) {
        viewModelScope.launch {
            val currentSave = _saveState.value
            
            val mappingList = if (currentSave.placedDecorsCsv.isEmpty()) {
                mutableListOf()
            } else {
                currentSave.placedDecorsCsv.split(",").filter { it.isNotEmpty() }.toMutableList()
            }
            
            // Remove existing mapping for this tableId
            mappingList.removeAll { it.startsWith("$tableId:") }
            
            if (decorKey != null) {
                // Remove this decor from any other table (each decor is placeable in one slot only)
                mappingList.removeAll { it.endsWith(":$decorKey") }
                // Add new mapping
                mappingList.add("$tableId:$decorKey")
                
                // Add particle
                val decor = DecorType.fromKey(decorKey)
                decor?.let {
                    addParticle(
                        emoji = it.emoji,
                        color = Color(0xFF9B59B6),
                        label = "Placed ${it.displayName}!",
                        x = (tableId * 22f),
                        y = 35f
                    )
                }
            } else {
                addParticle(
                    emoji = "💨",
                    color = Color.Gray,
                    label = "Removed decor",
                    x = (tableId * 22f),
                    y = 35f
                )
            }
            
            val newPlacedCsv = mappingList.joinToString(",")
            val updatedSave = currentSave.copy(placedDecorsCsv = newPlacedCsv)
            repository.saveProgress(updatedSave)
        }
    }

    fun getCatHappiness(tableId: Int, saveState: SaveState): Int {
        val decor = getPlacedDecorForTable(tableId, saveState)
        return 100 + (decor?.happinessBonus ?: 0)
    }

    // Simple Particles manager ticks block
    private fun addParticle(emoji: String, color: Color, label: String, x: Float, y: Float) {
        val newParticle = FloatingParticle(
            emoji = emoji,
            color = color,
            label = label,
            x = x,
            y = y
        )
        _particles.value = _particles.value + newParticle
    }

    private fun tickParticles() {
        _particles.value = _particles.value.mapNotNull { particle ->
            val nextAge = particle.ageTicks + 1
            if (nextAge > 20) { // Keep sparkles alive for 2 seconds
                null
            } else {
                particle.copy(
                    y = particle.y - 1.2f, // Float upwards
                    ageTicks = nextAge,
                    scale = 1.0f + (nextAge * 0.05f)
                )
            }
        }
    }
}
