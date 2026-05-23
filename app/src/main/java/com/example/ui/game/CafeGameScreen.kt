package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.SaveState
import kotlin.math.roundToInt

// Custom colors for Pixel aesthetic
val PixelCoffeeBrown = Color(0xFF4A341E)
val PixelGoldenSun = Color(0xFFF1C40F)
val PixelGrassGreen = Color(0xFF2ECC71)
val PixelSunsetOrange = Color(0xFFE67E22)
val PixelCreamBg = Color(0xFFFFFAEE)
val PixelCushionPink = Color(0xFFFD79A8)

@Composable
fun CafeGameRoot(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    Surface(
        color = PixelCreamBg,
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AnimatedContent(
            targetState = screenState,
            label = "ScreenNavigator",
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            }
        ) { targetScreen ->
            when (targetScreen) {
                GameScreen.MENU -> MainMenuScreen(
                    saveState = saveState,
                    onStartClick = { viewModel.navigateTo(GameScreen.PLAYING) },
                    onShopClick = { viewModel.navigateTo(GameScreen.SHOP) },
                    onResetClick = { viewModel.resetGameProgress() }
                )
                GameScreen.PLAYING -> ActiveGamePlayScreen(
                    viewModel = viewModel,
                    saveState = saveState
                )
                GameScreen.SHIFT_SUMMARY -> ShiftSummaryScreen(
                    viewModel = viewModel,
                    saveState = saveState
                )
                GameScreen.SHOP -> UpgradeShopScreen(
                    viewModel = viewModel,
                    saveState = saveState,
                    onBackClick = { viewModel.navigateTo(GameScreen.MENU) }
                )
                GameScreen.GAMEOVER -> GameOverScreen(
                    onRestartClick = { viewModel.navigateTo(GameScreen.PLAYING) },
                    onBackMenuClick = { viewModel.navigateTo(GameScreen.MENU) }
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    saveState: SaveState,
    onStartClick: () -> Unit,
    onShopClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF0D4), PixelCreamBg, Color(0xFFFFEAA7))
                )
            )
    ) {
        // Decorative pixel design blocks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            // Draw grid elements for retro theme
            val step = 45f
            for (x in 0 until (width / step).toInt()) {
                drawLine(
                    color = Color(0xFFFFE0B2).copy(alpha = 0.35f),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, height),
                    strokeWidth = 2f
                )
            }
            for (y in 0 until (height / step).toInt()) {
                drawLine(
                    color = Color(0xFFFFE0B2).copy(alpha = 0.35f),
                    start = Offset(0f, y * step),
                    end = Offset(width, y * step),
                    strokeWidth = 2f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Retro Board Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = "CAT CAFÉ",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = PixelCoffeeBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "🐾 PIXEL RUSH 🐾",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PixelSunsetOrange,
                    textAlign = TextAlign.Center
                )
            }

            // Central Pixel art/Splash
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(5.dp, PixelCoffeeBrown, RoundedCornerShape(24.dp))
                    .shadow(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_cat_cafe_splash_1779495137280),
                    contentDescription = "Cat Cafe Splash Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Stats Board Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9EE)),
                border = BorderStroke(3.dp, PixelCoffeeBrown),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🪙 Total Coins", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                        Text("${saveState.coins}", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelCoffeeBrown)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💖 Cat Love", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                        Text("${saveState.catLove}", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelCushionPink)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆 High Score", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                        Text("${saveState.highScore}", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelGoldenSun)
                    }
                }
            }

            // Actions Block
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(2.dp, PixelCoffeeBrown),
                    colors = ButtonDefaults.buttonColors(containerColor = PixelSunsetOrange),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(
                        "START NEW SHIFT (DAY ${saveState.currentDay})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onShopClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PixelCoffeeBrown)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Shop")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COZY SHOP", fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onResetClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(2.dp, Color.Red)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET PROGRESS", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveGamePlayScreen(
    viewModel: GameViewModel,
    saveState: SaveState
) {
    val coinsToday by viewModel.coinsEarnedToday.collectAsStateWithLifecycle()
    val loveToday by viewModel.loveEarnedToday.collectAsStateWithLifecycle()
    val lives by viewModel.lives.collectAsStateWithLifecycle()
    val remainingTime by viewModel.shiftTimeLeft.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val traySlots by viewModel.traySlots.collectAsStateWithLifecycle()
    val brewStations by viewModel.brewStations.collectAsStateWithLifecycle()
    val particles by viewModel.particles.collectAsStateWithLifecycle()

    val draggedIdx by viewModel.draggedItemIndex.collectAsStateWithLifecycle()
    val dragOffset by viewModel.dragOffset.collectAsStateWithLifecycle()

    // Screen positions mapped for drop collisions
    val tablePositions = remember { mutableMapOf<Int, Offset>() }

    // Tap Select fallbacks (perfect for precise serves + alternative control)
    var selectedTrayIndex by remember { mutableStateOf<Int?>(null) }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val slotPositions = remember { mutableMapOf<Int, Offset>() }
    var showDecorDialogForTableId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFDF8), Color(0xFFFBEADB), Color(0xFFFFFAEE))
                )
            )
            .pointerInput(Unit) {
                // Main visual tap gesture reset if they tap on empty background
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { _, dragAmount -> },
                    onDragEnd = { },
                    onDragCancel = { }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP STATUS HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shift Day Indicator
                Column {
                    Text(
                        "SHIFT DAY ${saveState.currentDay}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = PixelCoffeeBrown
                    )
                    // Shift Time Bar progress
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = "Clock", modifier = Modifier.size(14.dp), tint = PixelCoffeeBrown)
                        Spacer(modifier = Modifier.width(4.dp))
                        LinearProgressIndicator(
                            progress = { remainingTime / 85.0f },
                            modifier = Modifier
                                .width(90.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PixelSunsetOrange,
                            trackColor = Color(0xFFE5D5C4)
                        )
                    }
                }

                // Coin/Pettings status board
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFF9EE), RoundedCornerShape(8.dp))
                            .border(1.dp, PixelCoffeeBrown, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$coinsToday", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelCoffeeBrown)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFF9EE), RoundedCornerShape(8.dp))
                            .border(1.dp, PixelCoffeeBrown, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("💖", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$loveToday", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelCushionPink)
                    }
                }

                // Reputation Leaves/Hearts
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (i in 1..5) {
                        Text(
                            text = "❤️",
                            fontSize = 16.sp,
                            modifier = Modifier.alpha(if (i <= lives) 1.0f else 0.2f)
                        )
                    }
                }
            }

            // MIDDLE PLAYFIELD - CUSTOMER TABLES (Drag Drop Destination & Cat Petting Area)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tables) { table ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .onGloballyPositioned { layoutCoordinates ->
                                tablePositions[table.id] = layoutCoordinates.positionInWindow()
                            }
                    ) {
                        if (table.isUnlocked) {
                            TableLayout(
                                table = table,
                                saveState = saveState,
                                viewModel = viewModel,
                                onPetClick = { viewModel.petCat(table.id) },
                                isTrayItemSelectedSelected = selectedTrayIndex != null,
                                onServeWithSelectedClick = {
                                    val sel = selectedTrayIndex
                                    if (sel != null) {
                                        viewModel.stopDraggingAndServe(table.id)
                                        selectedTrayIndex = null
                                    }
                                },
                                onDecorClick = {
                                    showDecorDialogForTableId = table.id
                                }
                            )
                        } else {
                            LockedTableLayout(table = table)
                        }
                    }
                }
            }

            // TRAY CONTAINER (Hold prepared goods to serve)
            Card(
                colors = CardDefaults.cardColors(containerColor = PixelCoffeeBrown.copy(alpha = 0.08f)),
                border = BorderStroke(2.dp, PixelCoffeeBrown),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        "SERVING TRAY COUNTER (DRAG ITEM OR TAP TO SELECT)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = PixelCoffeeBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 4 Slots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            traySlots.forEachIndexed { index, item ->
                                val isSelected = selectedTrayIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFD2D2) else Color(0xFFFFFAEE))
                                        .border(
                                            width = if (isSelected) 3.dp else 2.dp,
                                            color = if (isSelected) Color.Red else PixelCoffeeBrown,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .pointerInput(index) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    selectedTrayIndex = null
                                                    viewModel.startDragging(index)
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    viewModel.onDrag(dragAmount)
                                                },
                                                onDragEnd = {
                                                    // Determine release coordinates and resolve which table it dropped on
                                                    var targetTableId: Int? = null
                                                    // A simple estimation of drag offset mapping relative to screen sectors
                                                    if (dragOffset.y < -380f) targetTableId = 1
                                                    else if (dragOffset.y < -240f) targetTableId = 2
                                                    else if (dragOffset.y < -120f) targetTableId = 3
                                                    else if (dragOffset.y < -20f) targetTableId = 4

                                                    viewModel.stopDraggingAndServe(targetTableId)
                                                },
                                                onDragCancel = {
                                                    viewModel.stopDraggingAndServe(null)
                                                }
                                            )
                                        }
                                        .clickable {
                                            if (item != null) {
                                                selectedTrayIndex = if (isSelected) null else index
                                                if (selectedTrayIndex != null) {
                                                    viewModel.startDragging(index)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(item.emoji, fontSize = 24.sp)
                                            Text(item.displayName.substring(0, Math.min(5, item.displayName.length)), fontSize = 8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                                        }
                                    } else {
                                        Text("+", fontSize = 18.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Double Tap Discard / Trash Box
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    val sel = selectedTrayIndex
                                    if (sel != null) {
                                        viewModel.discardTrayItem(sel)
                                        selectedTrayIndex = null
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFFFF0ED), RoundedCornerShape(8.dp))
                                    .border(2.dp, Color.Red, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Discard chosen item", tint = Color.Red, modifier = Modifier.size(24.dp))
                            }
                            Text("TRASH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Red, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // KITCHEN WORKSTATIONS
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, PixelCoffeeBrown),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        "KITCHEN PIXEL WORKSTATIONS (TAP TO BREW / BAKE)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PixelCoffeeBrown,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        brewStations.forEach { station ->
                            val speedLevel = when (station.itemType) {
                                CafeItem.COFFEE, CafeItem.MATCHA -> saveState.upgradeCoffeeOvenLevel
                                CafeItem.CAKE -> saveState.upgradePastryOvenLevel
                                CafeItem.TOY_FISH, CafeItem.TOY_YARN -> saveState.upgradeCatToysLevel
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (station.isBrewing) Color(0xFFE2F0D9) else Color(0xFFFFF9EE))
                                    .border(1.5.dp, PixelCoffeeBrown, RoundedCornerShape(8.dp))
                                    .clickable(enabled = !station.isBrewing) {
                                        viewModel.triggerPrep(station.id)
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(station.itemType.emoji, fontSize = 22.sp)
                                    Text(
                                        text = station.itemType.displayName.split(" ").last(),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = PixelCoffeeBrown
                                    )

                                    if (station.isBrewing) {
                                        LinearProgressIndicator(
                                            progress = { station.progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .padding(top = 2.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = PixelGrassGreen,
                                            trackColor = Color(0xFFD6E3D1)
                                        )
                                    } else {
                                        Text(
                                            "Lv.$speedLevel [Tap]",
                                            fontSize = 7.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // PARTICLES FLOATING OVERLAY
        particles.forEach { particle ->
            Text(
                text = "${particle.emoji} ${particle.label}",
                fontSize = (12 * particle.scale).sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = particle.color,
                modifier = Modifier
                    .offset(
                        x = (particle.x).dp * 3.5f,
                        y = (particle.y).dp * 4.5f
                    )
                    .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // FLOATING DRAGGED ICON OVERLAY
        if (draggedIdx != null) {
            val draggedItem = traySlots[draggedIdx!!]
            if (draggedItem != null) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                dragOffset.x.roundToInt() + 100,
                                dragOffset.y.roundToInt() + 850 // map relative starting counter position
                            )
                        }
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .border(3.dp, PixelSunsetOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(draggedItem.emoji, fontSize = 32.sp)
                }
            }
        }

        // DECORATIONS PLACEMENT OVERLAY DIALOG FOR MOBILE
        if (showDecorDialogForTableId != null) {
            val tableId = showDecorDialogForTableId!!
            val currentTable = tables.find { it.id == tableId }
            val currentlyPlaced = viewModel.getPlacedDecorForTable(tableId, saveState)
            val inventory = viewModel.getPurchasedDecors(saveState)

            AlertDialog(
                onDismissRequest = { showDecorDialogForTableId = null },
                title = {
                    Text(
                        "Decorate ${currentTable?.cat?.name ?: "Cat"}'s Cozy Spot! 🛋️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PixelCoffeeBrown
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Select cozy beds, climbing trees, or decorations to elevate feline warmth! High cat happiness reduces customer patience decay rate.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (currentlyPlaced != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF3EC)),
                                border = BorderStroke(1.5.dp, PixelCoffeeBrown),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(currentlyPlaced.emoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(currentlyPlaced.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelCoffeeBrown)
                                            Text("feline warmth +${currentlyPlaced.happinessBonus}%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = PixelCushionPink)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.placeDecor(tableId, null)
                                            showDecorDialogForTableId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(28.dp).align(Alignment.CenterVertically)
                                    ) {
                                        Text("Remove", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                                    }
                                }
                            }
                        }

                        Text("Your Inventory:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = PixelCoffeeBrown, modifier = Modifier.padding(bottom = 6.dp))

                        if (inventory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .border(1.5.dp, PixelCoffeeBrown.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                    Text("No decorations in stock yet! 🙀", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                    Button(
                                        onClick = {
                                            showDecorDialogForTableId = null
                                            viewModel.navigateTo(GameScreen.SHOP)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PixelSunsetOrange),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(top = 8.dp).height(30.dp)
                                    ) {
                                        Text("Go to Cozy Shop", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            val availableItems = inventory.filter { it != currentlyPlaced }
                            if (availableItems.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("All stock currently placed! Buy more in Shop.", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 180.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(availableItems) { item ->
                                        // Find if this item is currently placed on some other table
                                        val otherPlacedTableId = tables.find { tbl ->
                                            viewModel.getPlacedDecorForTable(tbl.id, saveState) == item
                                        }?.id

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, PixelCoffeeBrown.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(item.emoji, fontSize = 24.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(item.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PixelCoffeeBrown)
                                                        Text(
                                                            text = if (otherPlacedTableId != null) "At Table $otherPlacedTableId (will relocate)" else "In Inventory (Ready)",
                                                            fontSize = 8.sp,
                                                            color = if (otherPlacedTableId != null) PixelSunsetOrange else PixelGrassGreen,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.placeDecor(tableId, item.key)
                                                        showDecorDialogForTableId = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = PixelGrassGreen),
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Place", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDecorDialogForTableId = null }) {
                        Text("Cancel", fontFamily = FontFamily.Monospace, color = PixelCoffeeBrown, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun TableLayout(
    table: TableState,
    saveState: SaveState,
    viewModel: GameViewModel,
    onPetClick: () -> Unit,
    isTrayItemSelectedSelected: Boolean,
    onServeWithSelectedClick: () -> Unit,
    onDecorClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = if (table.isPettingFeedbackActive) 3.dp else 1.5.dp,
                color = if (table.isPettingFeedbackActive) PixelCushionPink else PixelCoffeeBrown,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = table.cat.color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Resident Cat & Pet Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1.1f)
                    .background(Color.White.copy(alpha = 0.61f), RoundedCornerShape(10.dp))
                    .border(1.dp, PixelCoffeeBrown.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { onPetClick() }
                    .padding(6.dp)
            ) {
                // Animated Cat Tail swing/sleeping logic
                val infiniteTransition = rememberInfiniteTransition(label = "TailSwing")
                val angle by infiniteTransition.animateFloat(
                    initialValue = -12f,
                    targetValue = 12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "SwingVal"
                )

                Text(
                    text = table.cat.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.rotate(if (table.isPettingFeedbackActive) angle * 2f else angle)
                )

                Text(
                    text = "PET ${table.cat.name.uppercase()}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PixelCoffeeBrown
                )
                Text(
                    text = "Slows Patience!",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Middle Decor Slot Placement Area
            val placedDecor = viewModel.getPlacedDecorForTable(table.id, saveState)
            val happinessRating = viewModel.getCatHappiness(table.id, saveState)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1.0f)
                    .height(86.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .border(
                        BorderStroke(
                            1.2.dp,
                            if (placedDecor != null) PixelCoffeeBrown else Color.LightGray.copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onDecorClick() }
                    .padding(5.dp)
            ) {
                if (placedDecor != null) {
                    Text(placedDecor.emoji, fontSize = 20.sp)
                    Text(
                        text = placedDecor.displayName.split(" ").firstOrNull() ?: "",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PixelCoffeeBrown,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$happinessRating%💖",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PixelCushionPink,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text("➕", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "DECOR",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "100%💖",
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Customer Order Bubble and Patience
            Column(
                modifier = Modifier.weight(1.9f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (table.customer != null) {
                    val customer = table.customer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${customer.avatarEmoji} ${customer.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PixelCoffeeBrown
                            )
                            Text(
                                text = "Ordered: ${customer.orderedItem.displayName}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                        }

                        // Order balloon bubble
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.5.dp, PixelSunsetOrange, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(customer.orderedItem.emoji, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Patience details
                    val progressRatio = customer.patience / customer.maxPatience
                    val progressColor = when {
                        progressRatio > 0.55f -> PixelGrassGreen
                        progressRatio > 0.30f -> PixelGoldenSun
                        else -> Color.Red
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Patience",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                            Text(
                                "${customer.patience.roundToInt()}s Left",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = progressColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = Color(0xFFF1F0EA)
                        )
                    }

                    // Serves Click helper alternative
                    if (isTrayItemSelectedSelected) {
                        Button(
                            onClick = onServeWithSelectedClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PixelGrassGreen),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .padding(top = 2.dp)
                        ) {
                            Text(
                                "TAP HERE TO SERVE",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                } else {
                    // Empty Table placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Table Empty. Awaiting customer... 💤",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockedTableLayout(table: TableState) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Lock, contentDescription = "Locked Table", tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TABLE UNLOCKED BY ADOPTING ${table.cat.name.uppercase()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray
            )
            Text(
                text = "Buy in Cozy Shop for ${table.cat.cost} Coins",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ShiftSummaryScreen(
    viewModel: GameViewModel,
    saveState: SaveState
) {
    val coinsEarned by viewModel.coinsEarnedToday.collectAsStateWithLifecycle()
    val loveEarned by viewModel.loveEarnedToday.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEAFFEA), PixelCreamBg)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text(
                "🐾 SHIFT COMPLETE! 🐾",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = PixelCoffeeBrown
            )
            Text(
                "The cat café closes for tonight. The cats are cozy and full!",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Summary Board
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(4.dp, PixelCoffeeBrown),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "SHIFT STATS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PixelSunsetOrange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🪙 Coins Served:", fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    Text("+$coinsEarned", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = PixelSunsetOrange)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💖 Purr-Pettings Love:", fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    Text("+$loveEarned", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = PixelCushionPink)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💰 Total Bank Coins:", fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    Text("${saveState.coins}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = PixelCoffeeBrown)
                }
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.navigateTo(GameScreen.SHOP) },
                colors = ButtonDefaults.buttonColors(containerColor = PixelCoffeeBrown),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Shop")
                Spacer(modifier = Modifier.width(6.dp))
                Text("COZY UPGRADE SHOP", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.startNextDay() },
                colors = ButtonDefaults.buttonColors(containerColor = PixelGrassGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("START NEXT DAY (DAY ${saveState.currentDay + 1})", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UpgradeShopScreen(
    viewModel: GameViewModel,
    saveState: SaveState,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelCreamBg)
            .padding(20.dp)
    ) {
        // Shop Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PixelCoffeeBrown)
            }
            Text(
                "🐈 COZY UPGRADES 🐈",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = PixelCoffeeBrown
            )
            // Coin tracker in Shop
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, PixelCoffeeBrown, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text("🪙", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text("${saveState.coins}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        var selectedTab by remember { mutableStateOf(0) } // 0 = Upgrades, 1 = Cute Decorations

        // Retro Tab Selection Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) PixelCoffeeBrown else Color.White,
                    contentColor = if (selectedTab == 0) Color.White else PixelCoffeeBrown
                ),
                border = BorderStroke(2.dp, PixelCoffeeBrown),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text("🛠️ UPGRADES", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) PixelCoffeeBrown else Color.White,
                    contentColor = if (selectedTab == 1) Color.White else PixelCoffeeBrown
                ),
                border = BorderStroke(2.dp, PixelCoffeeBrown),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text("🧸 COZY SHOP", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Menu of upgrades or decorations
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (selectedTab == 0) {
                // Adopt Table 3 / Mochi
                item {
                    UpgradeItemRow(
                        title = "Adopt Mochi & Unlock Table 3",
                        description = "Mochi is a fluffy Calico. Adopting Mochi unlocks Table 3 to serve 50% more customers!",
                        emoji = "😻",
                        cost = 200,
                        unlocked = saveState.unlockedTables >= 3,
                        currentCoins = saveState.coins,
                        onBuyClick = { viewModel.buyUpgrade("table_3") }
                    )
                }

                // Adopt Table 4 / Shadow
                item {
                    UpgradeItemRow(
                        title = "Adopt Shadow & Unlock Table 4",
                        description = "Shadow is an affectionate black cat. Adds Table 4 so your café thrives at max capacity!",
                        emoji = "🐈‍⬛",
                        cost = 500,
                        unlocked = saveState.unlockedTables >= 4,
                        currentCoins = saveState.coins,
                        onBuyClick = { viewModel.buyUpgrade("table_4") }
                    )
                }

                // Upgrade Espresso Machine Coffee Speed
                item {
                    val nextCost = saveState.upgradeCoffeeOvenLevel * 100
                    UpgradeItemRow(
                        title = "Dual-Nozzle Espresso Machine (Lv. ${saveState.upgradeCoffeeOvenLevel})",
                        description = "Speeds up Espresso & Matcha tea generation by 15% and increases their sell value!",
                        emoji = "☕",
                        cost = nextCost,
                        unlocked = false,
                        currentCoins = saveState.coins,
                        onBuyClick = { viewModel.buyUpgrade("coffee_speed") }
                    )
                }

                // Upgrade Pastry Oven Cake Speed
                item {
                    val nextCost = saveState.upgradePastryOvenLevel * 120
                    UpgradeItemRow(
                        title = "Turbo Sweet Convection Oven (Lv. ${saveState.upgradePastryOvenLevel})",
                        description = "Speeds up baking Cat Shortcakes by 15% and increases slice coins payout!",
                        emoji = "🍰",
                        cost = nextCost,
                        unlocked = false,
                        currentCoins = saveState.coins,
                        onBuyClick = { viewModel.buyUpgrade("pastry_speed") }
                    )
                }

                // Cat Toys / Overall Cushion Patience
                item {
                    val nextCost = saveState.upgradeCatToysLevel * 80
                    UpgradeItemRow(
                        title = "Premium Cushion & Yarn Station (Lv. ${saveState.upgradeCatToysLevel})",
                        description = "Boosts customers' base starting patience by +8 seconds and speeds up toy assembly!",
                        emoji = "🧶",
                        cost = nextCost,
                        unlocked = false,
                        currentCoins = saveState.coins,
                        onBuyClick = { viewModel.buyUpgrade("toys_patience") }
                    )
                }
            } else {
                items(DecorType.values()) { decor ->
                    val isPurchased = viewModel.getPurchasedDecors(saveState).contains(decor)
                    UpgradeItemRow(
                        title = "${decor.displayName} [${decor.category}]",
                        description = "${decor.description} Boosts table cat happiness +${decor.happinessBonus}%.",
                        emoji = decor.emoji,
                        cost = decor.cost,
                        unlocked = isPurchased,
                        currentCoins = saveState.coins,
                        onBuyClick = { viewModel.buyDecor(decor) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.navigateTo(GameScreen.PLAYING) },
            colors = ButtonDefaults.buttonColors(containerColor = PixelSunsetOrange),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("START SHIFT WITH COZY UPGRADES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UpgradeItemRow(
    title: String,
    description: String,
    emoji: String,
    cost: Int,
    unlocked: Boolean,
    currentCoins: Int,
    onBuyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, PixelCoffeeBrown),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFFFFAEE), RoundedCornerShape(8.dp))
                    .border(1.dp, PixelCoffeeBrown, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PixelCoffeeBrown
                )
                Text(
                    text = description,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                    lineHeight = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Purchase/Adopt Action Button
            if (unlocked) {
                Text(
                    "ADOPTED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PixelGrassGreen,
                    modifier = Modifier.padding(6.dp)
                )
            } else {
                val canAfford = currentCoins >= cost
                Button(
                    onClick = onBuyClick,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelSunsetOrange,
                        disabledContainerColor = Color.LightGray
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "🪙 $cost",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(
    onRestartClick: () -> Unit,
    onBackMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF0ED), PixelCreamBg)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("😿", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "SHIFT GOT TOO HECTIC!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "The customer queue got too busy and our cats got overwhelmed/sleepy! Don't fret, let's calm them down and try again.",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRestartClick,
                colors = ButtonDefaults.buttonColors(containerColor = PixelSunsetOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("RETRY THIS SHIFT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBackMenuClick,
                border = BorderStroke(2.dp, PixelCoffeeBrown),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("RETURN TO COZY MANOR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PixelCoffeeBrown)
            }
        }
    }
}

// Simple legacy wrapper
@Composable
fun Image(
    painter: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    androidx.compose.foundation.Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
