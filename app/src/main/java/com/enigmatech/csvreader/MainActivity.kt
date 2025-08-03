package com.enigmatech.csvreader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.enigmatech.csvreader.impl.CsvLineParser
import com.enigmatech.csvreader.impl.DefaultNormalizer
import com.enigmatech.csvreader.impl.DefaultTransformer
import com.enigmatech.csvreader.impl.ISchemaParser
import com.enigmatech.csvreader.impl.Normalizer
import com.enigmatech.csvreader.impl.RenderEngineRegistry
import com.enigmatech.csvreader.ui.theme.CSVReaderTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var renderEngineRegistry: RenderEngineRegistry
    @Inject lateinit var schemaParser: ISchemaParser
    @Inject lateinit var normalizer: Normalizer

    private val configJsonUrl = "https://uscqcitfahobrkhozugc.supabase.co/storage/v1/object/public/configs/config_json.json"
    private val configXmlUrl = "https://uscqcitfahobrkhozugc.supabase.co/storage/v1/object/public/configs/config_xml.json"

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Composable
        fun DropdownMenu(current: String, onSelected: (String) -> Unit) {
            var expanded by remember { mutableStateOf(false) }
            val options = listOf("json", "xml")

            Box {
                Button(onClick = { expanded = true }) {
                    Text("Format: ${current.uppercase()}")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.uppercase()) },
                            onClick = {
                                onSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }


        setContent {
            CSVReaderTheme {
                var selectedFormat by remember { mutableStateOf("json") }
                var normalizedOutput by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var errorMsg by remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        lifecycleScope.launch {
                            isLoading = true
                            try {
                                val configUrl = if (selectedFormat == "json") configJsonUrl else configXmlUrl
                                val configString = downloadFromUrl(configUrl)

                                val config = schemaParser.parse(configString)
                                val csvLines = readCsvFromUri(uri)
                                val parsedMap = CsvLineParser().parseLines(csvLines)
                                val normalizedMap = normalizer.normalize(parsedMap, config)
                                val output = renderEngineRegistry.render(selectedFormat, normalizedMap)
                                normalizedOutput = output
                            } catch (e: Exception) {
                                errorMsg = e.message
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("CSV Normalizer") },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text("Select Output Format:")
                            Spacer(modifier = Modifier.height(8.dp))
                            DropdownMenu(selectedFormat) { selected ->
                                selectedFormat = selected
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = {
                                filePickerLauncher.launch("text/*")
                            }) {
                                Text("Pick CSV File")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isLoading) {
                                CircularProgressIndicator()
                            }

                            errorMsg?.let {
                                Text("Error: $it", color = MaterialTheme.colorScheme.error)
                            }

                            if (normalizedOutput.isNotBlank()) {
                                Text("Formatted Output:", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    BasicTextField(
                                        value = normalizedOutput,
                                        onValueChange = {},
                                        readOnly = true,
                                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onBackground)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(onClick = {
                                    saveTextToDownload(normalizedOutput, selectedFormat)
                                }) {
                                    Text("Download Output")
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private suspend fun downloadFromUrl(url: String): String = withContext(Dispatchers.IO) {
        URL(url).readText()
    }

    private suspend fun readCsvFromUri(uri: Uri): List<String> = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(uri)?.bufferedReader()?.readLines() ?: emptyList()
    }

    private fun saveTextToDownload(content: String, format: String) {
        val fileName = "output.$format"
        val file = File(cacheDir, fileName)
        file.writeText(content)
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share output file"))
    }
}

