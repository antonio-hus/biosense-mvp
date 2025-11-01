package com.biosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biosense.app.ui.theme.BiosenseTheme

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiosenseTheme {
                PrivacyPolicyScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    TextButton(onClick = onBackPressed) {
                        Text("Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "BioSense Health Data Privacy Policy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "How We Use Your Health Data",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "BioSense requests access to your health data to provide personalized health insights and tracking. We use this data to:",
                fontSize = 14.sp
            )
            
            Column(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("• Track your daily activity and fitness metrics", fontSize = 14.sp)
                Text("• Monitor vital signs like heart rate and blood pressure", fontSize = 14.sp)
                Text("• Analyze sleep patterns and quality", fontSize = 14.sp)
                Text("• Provide personalized health recommendations", fontSize = 14.sp)
                Text("• Generate health trends and insights over time", fontSize = 14.sp)
            }
            
            Text(
                text = "Data Security",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Your health data is stored locally on your device and synchronized with Health Connect. We do not transmit your health data to external servers without your explicit consent. All data processing happens on your device to ensure maximum privacy.",
                fontSize = 14.sp
            )
            
            Text(
                text = "Data Sharing",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "We never sell or share your personal health data with third parties. Your data remains under your control, and you can revoke access at any time through the Health Connect settings.",
                fontSize = 14.sp
            )
            
            Text(
                text = "Your Rights",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "You have the right to:\n• Access your data at any time\n• Delete your data from our app\n• Revoke permissions through Health Connect\n• Export your data in standard formats",
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onBackPressed,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("I Understand")
            }
        }
    }
}