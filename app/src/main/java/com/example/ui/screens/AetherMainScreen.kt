package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.ui.components.AmorphousBlobBackground
import com.example.ui.components.BlobTheme
import com.example.ui.components.FrostedGlassBox
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.OpeningCreditScene
import com.example.ui.components.SoundwaveVisualizer
import com.example.ui.theme.GlassBorderColor
import com.example.ui.theme.MidnightBackground
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AetherViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AetherMainScreen(
    viewModel: AetherViewModel
) {
    val showCreditScene by viewModel.showCreditScene.collectAsStateWithLifecycle()

    if (showCreditScene) {
        OpeningCreditScene(
            onEnterApp = { viewModel.dismissCreditScene() }
        )
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentConvId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()

    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by viewModel.speakingMessageId.collectAsStateWithLifecycle()

    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    val attachedImage by viewModel.attachedImageBase64.collectAsStateWithLifecycle()
    val showMemorySheet by viewModel.showMemorySheet.collectAsStateWithLifecycle()
    val showPersonaSheet by viewModel.showPersonaSheet.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll down when new message arrives
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = conversations,
                currentConvId = currentConvId,
                onSelectConv = { id ->
                    viewModel.selectConversation(id)
                    scope.launch { drawerState.close() }
                },
                onNewChat = {
                    viewModel.createNewChat()
                    scope.launch { drawerState.close() }
                },
                onDeleteConv = { id -> viewModel.deleteConversation(id) }
            )
        }
    ) {
        AmorphousBlobBackground(theme = currentTheme) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopBarHeader(
                        currentPersona = currentPersona,
                        isThinking = isThinking,
                        memoryCount = memories.size,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onOpenPersonaSheet = { viewModel.togglePersonaSheet(true) },
                        onOpenMemorySheet = { viewModel.toggleMemorySheet(true) },
                        onReplayCredit = { viewModel.replayCreditScene() }
                    )
                },
                bottomBar = {
                    InputBottomBar(
                        textInput = textInput,
                        onTextInputChange = { textInput = it },
                        attachedImage = attachedImage,
                        onRemoveImage = { viewModel.setAttachedImage(null) },
                        onAttachSampleImage = {
                            // Demo image attach base64 dot pattern
                            viewModel.setAttachedImage("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==")
                            Toast.makeText(context, "Image attached! Ask Aether about it", Toast.LENGTH_SHORT).show()
                        },
                        onSend = {
                            if (textInput.isNotBlank() || attachedImage != null) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Chat Messages List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        items(messages, key = { it.id }) { msg ->
                            MessageItemCard(
                                message = msg,
                                isSpeaking = isSpeaking && speakingMessageId == msg.id,
                                onReadAloud = { viewModel.speakMessage(msg.id, msg.content) },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Aether Message", msg.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                onReaction = { reaction ->
                                    viewModel.toggleMessageReaction(msg.id, reaction)
                                }
                            )
                        }

                        // Thinking indicator
                        if (isThinking) {
                            item {
                                ThinkingIndicatorCard()
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // Icebreaker prompts chips when conversation is fresh
                    if (messages.size <= 2) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(viewModel.icebreakers) { prompt ->
                                IcebreakerChip(
                                    text = prompt,
                                    onClick = {
                                        viewModel.sendMessage(prompt.replace("✨ ", "").replace("💖 ", "").replace("💡 ", "").replace("☕ ", "").replace("🚀 ", ""))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Persona Sheet Modal
    if (showPersonaSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.togglePersonaSheet(false) },
            containerColor = MidnightDark,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            PersonaAndThemeSheet(
                currentPersona = currentPersona,
                currentTheme = currentTheme,
                onSelectPersona = { persona ->
                    viewModel.setPersona(persona)
                    viewModel.togglePersonaSheet(false)
                },
                onSelectTheme = { theme ->
                    viewModel.setTheme(theme)
                }
            )
        }
    }

    // Bestie Memory Notebook Sheet Modal
    if (showMemorySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleMemorySheet(false) },
            containerColor = MidnightDark,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            MemoryNotebookSheet(
                memories = memories,
                onAddMemory = { key, valStr -> viewModel.addMemory(key, valStr) },
                onDeleteMemory = { id -> viewModel.deleteMemory(id) }
            )
        }
    }
}

@Composable
fun TopBarHeader(
    currentPersona: String,
    isThinking: Boolean,
    memoryCount: Int,
    onOpenDrawer: () -> Unit,
    onOpenPersonaSheet: () -> Unit,
    onOpenMemorySheet: () -> Unit,
    onReplayCredit: () -> Unit
) {
    FrostedGlassBox(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        backgroundColor = Color(0xAA0D0F1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Conversations",
                    tint = TextPrimary
                )
            }

            // Avatar & Title Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenPersonaSheet() }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aether_avatar_1785739748130),
                        contentDescription = "Aether Avatar",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonCyan, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(if (isThinking) NeonPink else Color(0xFF00FFA3))
                            .border(1.5.dp, MidnightBackground, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aether",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3300F0FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentPersona,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                    Text(
                        text = if (isThinking) "Thinking..." else "Online • Bestie Companion",
                        fontSize = 11.sp,
                        color = if (isThinking) NeonPink else TextSecondary
                    )
                }
            }

            // Right Action Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenMemorySheet) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Memories",
                            tint = NeonCyan
                        )
                        if (memoryCount > 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(NeonPink)
                            ) {
                                Text(
                                    text = memoryCount.toString(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onOpenPersonaSheet) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Themes & Personas",
                        tint = TextPrimary
                    )
                }

                IconButton(onClick = onReplayCredit) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Replay Opening Scene",
                        tint = NeonPink
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    conversations: List<ConversationEntity>,
    currentConvId: Long?,
    onSelectConv: (Long) -> Unit,
    onNewChat: () -> Unit,
    onDeleteConv: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp),
        color = MidnightDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conversations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onNewChat) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = NeonPink
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == currentConvId
                    FrostedGlassCard(
                        onClick = { onSelectConv(conv.id) },
                        accentGlowColor = if (isSelected) NeonCyan else null,
                        backgroundColor = if (isSelected) Color(0x881E2342) else Color(0x33121526)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonCyan else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(conv.createdAt)),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            if (conversations.size > 1) {
                                IconButton(
                                    onClick = { onDeleteConv(conv.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItemCard(
    message: MessageEntity,
    isSpeaking: Boolean,
    onReadAloud: () -> Unit,
    onCopy: () -> Unit,
    onReaction: (String) -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!isUser) {
                Image(
                    painter = painterResource(id = R.drawable.ic_aether_avatar_1785739748130),
                    contentDescription = "Aether Avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, NeonCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            FrostedGlassCard(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 20.dp
                ),
                backgroundColor = if (isUser) Color(0x991E2445) else Color(0x77111427),
                accentGlowColor = if (isUser) NeonCyan else NeonPink
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (message.imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x4400F0FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Attached Image",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = TextMuted
                        )

                        // Action toolbar for Aether messages
                        if (!isUser) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSpeaking) {
                                    SoundwaveVisualizer(isPlaying = true)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                IconButton(
                                    onClick = onReadAloud,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Read Aloud",
                                        tint = if (isSpeaking) NeonPink else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Reaction Pill if set
                    if (message.reaction != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x44FF007A))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = message.reaction, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Reaction options bar under Aether messages
        if (!isUser) {
            Row(
                modifier = Modifier.padding(start = 40.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("❤️", "😂", "🤯", "⚡", "🫂").forEach { r ->
                    Text(
                        text = r,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onReaction(r) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicatorCard() {
    val transition = rememberInfiniteTransition(label = "ThinkingPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_aether_avatar_1785739748130),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, NeonPink, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FrostedGlassCard(
            shape = RoundedCornerShape(18.dp),
            accentGlowColor = NeonPink
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aether is thinking",
                    fontSize = 13.sp,
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonPink.copy(alpha = alpha))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IcebreakerChip(
    text: String,
    onClick: () -> Unit
) {
    FrostedGlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        accentGlowColor = NeonCyan
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun InputBottomBar(
    textInput: String,
    onTextInputChange: (String) -> Unit,
    attachedImage: String?,
    onRemoveImage: () -> Unit,
    onAttachSampleImage: () -> Unit,
    onSend: () -> Unit
) {
    FrostedGlassBox(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        backgroundColor = Color(0xCC0B0D1B),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (attachedImage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300F0FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Image attached",
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = NeonPink,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemoveImage() }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onAttachSampleImage) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach photo",
                        tint = NeonCyan
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = onTextInputChange,
                    placeholder = {
                        Text(
                            text = "Talk to your best friend Aether...",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onSend,
                    containerColor = NeonCyan,
                    contentColor = MidnightBackground,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonaAndThemeSheet(
    currentPersona: String,
    currentTheme: BlobTheme,
    onSelectPersona: (String) -> Unit,
    onSelectTheme: (BlobTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Bestie Personality & Vibe",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SELECT PERSONA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val personas = listOf(
            "SUPPORTIVE" to "🌟 Supportive Bestie",
            "WITTY" to "⚡ Witty & Sarcastic",
            "CREATIVE" to "🎨 Creative Spark",
            "DEEP_THINKER" to "🌙 Late Night Thinker",
            "HYPEMAN" to "🚀 Hypeman & Coach"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personas.forEach { (id, label) ->
                val isSelected = currentPersona == id
                FrostedGlassCard(
                    onClick = { onSelectPersona(id) },
                    accentGlowColor = if (isSelected) NeonPink else null,
                    backgroundColor = if (isSelected) Color(0x77FF007A) else Color(0x331E2342)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NEON LIQUID BLOBS THEME",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonPink,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val themes = listOf(
            BlobTheme.CYBER_NEON to "Cyber Neon (Cyan + Pink)",
            BlobTheme.AURORA_GLOW to "Aurora Glow (Emerald + Violet)",
            BlobTheme.MIDNIGHT_GLASS to "Midnight Glass (Deep Violet)",
            BlobTheme.SOLAR_ECLIPSE to "Solar Eclipse (Amber + Coral)"
        )

        themes.forEach { (theme, label) ->
            val isSelected = currentTheme == theme
            FrostedGlassCard(
                onClick = { onSelectTheme(theme) },
                accentGlowColor = if (isSelected) NeonCyan else null,
                backgroundColor = if (isSelected) Color(0x7700F0FF) else Color(0x331E2342),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MemoryNotebookSheet(
    memories: List<com.example.data.local.MemoryEntity>,
    onAddMemory: (String, String) -> Unit,
    onDeleteMemory: (Long) -> Unit
) {
    var keyInput by remember { mutableStateOf("") }
    var valInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = NeonCyan
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Aether's Bestie Memory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Text(
            text = "Facts Aether remembers about you to personalize conversations.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Form
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                placeholder = { Text("Topic (e.g., Favorite Food)", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
            )
            OutlinedTextField(
                value = valInput,
                onValueChange = { valInput = it },
                placeholder = { Text("Detail (e.g., Spicy Ramen)", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FloatingActionButton(
            onClick = {
                if (keyInput.isNotBlank() && valInput.isNotBlank()) {
                    onAddMemory(keyInput, valInput)
                    keyInput = ""
                    valInput = ""
                }
            },
            containerColor = NeonPink,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memory")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(memories, key = { it.id }) { mem ->
                FrostedGlassCard(
                    backgroundColor = Color(0x441E2342)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = mem.memoryKey,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = mem.memoryValue,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        IconButton(onClick = { onDeleteMemory(mem.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
