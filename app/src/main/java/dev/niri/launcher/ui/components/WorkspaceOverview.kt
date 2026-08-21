package dev.niri.launcher.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.niri.launcher.model.Workspace
import dev.niri.launcher.ui.theme.*

/**
 * Overview mode: shows all workspaces as miniaturized previews.
 * Activated by pinch gesture or the overview button in the top bar.
 */
@Composable
fun WorkspaceOverview(
    workspaces: List<Workspace>,
    currentWorkspace: Int,
    onWorkspaceSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
    ) {
        Box(modifier = modifier.fillMaxSize()
            .background(NoctBg.copy(alpha = 0.92f))
            .clickable(remember { MutableInteractionSource() }, null) { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp),
            ) {
                Text("工作区", color = NoctText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 24.dp))

                // Workspace cards in a row
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    workspaces.forEachIndexed { index, workspace ->
                        val isCurrent = index == currentWorkspace
                        WorkspaceMiniCard(
                            workspace = workspace,
                            index = index,
                            isCurrent = isCurrent,
                            onClick = { onWorkspaceSelect(index) },
                            modifier = Modifier.graphicsLayer {
                                scaleX = if (isCurrent) 1.05f else 0.95f
                                scaleY = if (isCurrent) 1.05f else 0.95f
                            },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Workspace labels
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    workspaces.forEachIndexed { index, _ ->
                        val isCurrent = index == currentWorkspace
                        Text(
                            text = "工作区 ${index + 1}",
                            color = if (isCurrent) NoctPrimary else NoctTextDim,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(80.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceMiniCard(
    workspace: Workspace,
    index: Int,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isCurrent) NoctPrimary else NoctBorder

    Column(
        modifier = modifier
            .width(120.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrent) NoctSurfaceHi else NoctSurface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        // Mini column indicators
        val colCount = workspace.columns.size.coerceAtMost(4)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            repeat(colCount) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isCurrent) NoctPrimary.copy(alpha = 0.2f) else NoctSurfaceHov))
            }
        }

        // Column count
        Text(
            text = "${workspace.columns.size} 列",
            color = NoctTextDim,
            fontSize = 9.sp,
        )
    }
}
