package com.noemi.streamer.screen

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.noemi.streamer.R
import com.noemi.streamer.model.PayloadData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MastodonApp() {
    Column {
        MastodonAppBar(title = stringResource(id = R.string.label_popular_stream), contentDescription = stringResource(id = R.string.label_icon_content_description))

        MastodonScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MastodonAppBar(title: String, contentDescription: String, modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = contentDescription,
                modifier = modifier.size(32.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun MastodonScreen(modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<MainViewModel>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val payloadsState by viewModel.payloadsState.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorState.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        SearchTextField(
            searchTerm = viewModel.searchTerm,
            onSearchTermChanged = viewModel::onSearchTermChanged,
            onGetPublicTimelines = viewModel::fetchPublicTimelines,
            isActiveNetwork = networkState
        )

        ScreenContent(payloads = payloadsState, isLoading = loadingState, isActiveNetwork = networkState)

        if (errorMessage.isNotBlank()) {

            var stateValue by remember { mutableStateOf(false) }

            scope.launch {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                stateValue = true
            }

            if (stateValue && networkState) viewModel.reFetchPublicTimelines()
        }
    }
}

@Composable
private fun SearchTextField(
    searchTerm: String,
    onSearchTermChanged: (String) -> Unit,
    onGetPublicTimelines: (String) -> Unit,
    isActiveNetwork: Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(60)
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        modifier = modifier
            .focusRequester(focusRequester = focusRequester)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp)
            .testTag(stringResource(id = R.string.label_search_text_tag)),
        value = searchTerm,
        onValueChange = { query ->
            onSearchTermChanged(query)
        },
        label = { Text(text = stringResource(id = R.string.label_timeline)) },
        placeholder = {
            Text(
                text = stringResource(id = R.string.label_search_hint),
                style = MaterialTheme.typography.titleMedium
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrect = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.None
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            when (isActiveNetwork) {
                true -> onGetPublicTimelines(searchTerm)
                else -> Toast.makeText(context, R.string.label_no_internet_connection, Toast.LENGTH_LONG).show()
            }
        }),
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        textStyle = MaterialTheme.typography.titleMedium,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                modifier = modifier.clickable {
                    keyboardController?.hide()
                    onSearchTermChanged("")
                }
            )
        }
    )
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun ScreenContent(payloads: List<PayloadData>, isLoading: Boolean, isActiveNetwork: Boolean, modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val (progressIndicator, column) = createRefs()

        when (isLoading) {
            true ->
                CircularProgressIndicator(
                    modifier = modifier
                        .constrainAs(progressIndicator) {
                            linkTo(parent.top, parent.bottom)
                            linkTo(parent.start, parent.end)
                        }
                        .testTag(stringResource(id = R.string.label_progress_indicator_tag)),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 3.dp
                )

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 20.dp),
                    modifier = modifier
                        .constrainAs(column) {
                            linkTo(parent.start, parent.end)
                            linkTo(parent.top, parent.bottom)
                            height = Dimension.fillToConstraints
                        }
                        .testTag(stringResource(id = R.string.label_lazy_column_tag)),
                    state = lazyListState
                ) {

                    items(
                        items = payloads,
                        key = { it.id }
                    ) { stream ->
                        if (isActiveNetwork && !lazyListState.isScrollInProgress) {
                            scope.launch {
                                lazyListState.scrollToItem(0)
                            }
                        }

                        MastodonItemRow(payload = stream, isActiveNetwork = isActiveNetwork)
                    }
                }
            }
        }
    }
}

@Composable
private fun MastodonItemRow(payload: PayloadData, isActiveNetwork: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = MaterialTheme.shapes.large,
            modifier = modifier
                .padding(8.dp)
                .clickable {
                    when (isActiveNetwork) {
                        true -> CustomTabsIntent
                            .Builder()
                            .build()
                            .launchUrl(context, Uri.parse(payload.url))
                        else -> Toast
                            .makeText(context, R.string.label_no_internet_connection, Toast.LENGTH_LONG)
                            .show()
                    }

                }
        ) {
            Column {

                Row {

                    AsyncImage(
                        model = payload.account.avatar,
                        contentDescription = stringResource(id = R.string.label_user_avatar),
                        modifier = modifier
                            .size(width = 60.dp, height = 60.dp)
                            .padding(top = 8.dp, end = 8.dp, start = 8.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text = payload.account.username,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, end = 8.dp, start = 8.dp),
                        textAlign = TextAlign.Justify,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = HtmlCompat.fromHtml(payload.content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 6.dp),
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = payload.url,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
