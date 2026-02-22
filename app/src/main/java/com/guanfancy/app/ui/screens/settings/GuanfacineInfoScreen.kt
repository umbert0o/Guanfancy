package com.guanfancy.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuanfacineInfoScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Guanfacine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("Overview")
            BodyText(
                "Guanfacine, sold under the brand name Tenex (immediate-release) and Intuniv " +
                "(extended-release) among others, is an oral alpha-2a agonist medication used to " +
                "treat attention deficit hyperactivity disorder (ADHD) and high blood pressure."
            )
            BodyText(
                "It appears to work by activating α2A-adrenergic receptors in the brain, thereby " +
                "decreasing sympathetic nervous system activity."
            )
            
            SectionHeader("Medical Uses")
            SubsectionHeader("ADHD Treatment")
            BodyText(
                "Guanfacine XR (brand name Intuniv) is indicated for the treatment of ADHD, " +
                "primarily for hyperactive symptoms. It is used both as monotherapy and as " +
                "adjunctive therapy to stimulant medications."
            )
            BodyText(
                "For ADHD, guanfacine helps individuals better control behavior, inhibit " +
                "inappropriate distractions and impulses, and inhibit inappropriate aggressive " +
                "impulses. Unlike stimulant medications, guanfacine is regarded as having no " +
                "abuse potential."
            )
            BodyText(
                "Guanfacine and other α2-adrenergic receptor agonists are considered to be less " +
                "effective than stimulants in the treatment of ADHD, at least when compared on " +
                "rating scales developed to assess stimulant efficacy."
            )
            
            SubsectionHeader("Hypertension")
            BodyText(
                "Guanfacine IR (brand name Tenex) is FDA-approved for the management of hypertension."
            )
            
            SubsectionHeader("Off-Label Uses")
            BodyText(
                "Guanfacine is also used off-label to treat tic disorders, anxiety disorders " +
                "(such as generalized anxiety disorder), and PTSD. It has anxiolytic-like action, " +
                "reducing emotional responses of the amygdala and strengthening prefrontal cortical " +
                "regulation of emotion, action, and thought."
            )
            BodyText(
                "Due to its prolonged elimination half-life, it has been seen to improve sleep " +
                "interrupted by nightmares in PTSD patients. Other off-label indications include " +
                "drug withdrawals (opioid, nicotine, cocaine), migraine prophylaxis, and Fragile X " +
                "Syndrome."
            )
            
            SectionHeader("Side Effects")
            BodyText(
                "Common side effects include sleepiness, constipation, and dry mouth. Other side " +
                "effects may include low blood pressure and urinary problems."
            )
            
            SectionHeader("Interactions")
            BodyText(
                "Guanfacine availability is significantly affected by the CYP3A4 and CYP3A5 " +
                "enzymes. Medications that inhibit or induce those enzymes change the amount of " +
                "guanfacine in circulation and thus its efficacy and rate of adverse effects."
            )
            BodyText(
                "Because of its impact on the heart, it should be used with caution with other " +
                "cardioactive drugs. A similar concern is appropriate when used with sedating " +
                "medications."
            )
            
            SectionHeader("Pharmacokinetics")
            BodyText(
                "Guanfacine has an oral bioavailability of 80%. There is no clear evidence of " +
                "any first-pass metabolism. Its elimination half-life is approximately 17 hours " +
                "with the major elimination route being renal."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PharmacokineticTable()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader("History")
            BodyText(
                "Guanfacine was first described in 1974 and was approved for medical use in the " +
                "United States in 1986. It is available as a generic medication. In 2023, it was " +
                "the 263rd most commonly prescribed medication in the United States, with more " +
                "than 1 million prescriptions."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Source: Wikipedia (https://en.wikipedia.org/wiki/Guanfacine)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Pharmacokinetic data from FDA prescribing information",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SubsectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PharmacokineticTable() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Pharmacokinetic Data Comparison",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val tableData = listOf(
            Triple("Parameter", "Intuniv 1mg", "Tenex 1mg"),
            Triple("Cmax (ng/mL)", "1.0 ± 0.3", "2.5 ± 0.6"),
            Triple("AUC∞ (ng·h/mL)", "32 ± 9", "56 ± 15"),
            Triple("T½ (hours)", "18 ± 4", "16 ± 3"),
            Triple("Tmax (hours)", "6.0 (4.0-8.0)", "3.0 (1.5-4.0)"),
            Triple("Bioavailability", "58%", "80-100%")
        )
        
        tableData.forEachIndexed { index, (param, intuniv, tenex) ->
            TableRow(
                param = param,
                value1 = intuniv,
                value2 = tenex,
                isHeader = index == 0
            )
        }
    }
}

@Composable
private fun TableRow(
    param: String,
    value1: String,
    value2: String,
    isHeader: Boolean
) {
    val style = if (isHeader) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.bodySmall
    }
    val color = if (isHeader) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = param,
            style = style,
            color = color,
            modifier = Modifier.weight(1f),
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value1,
            style = style,
            color = color,
            modifier = Modifier.weight(0.7f)
        )
        Text(
            text = value2,
            style = style,
            color = color,
            modifier = Modifier.weight(0.7f)
        )
    }
}
