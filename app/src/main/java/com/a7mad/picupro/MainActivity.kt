package com.a7mad.picupro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.a7mad.picupro.security.ActivationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isActivated = ActivationManager.isActivated(this)

        setContent {
            val navController = rememberNavController()
            val startDestination = if (isActivated) "drug_list" else "activation"

            NavHost(navController = navController, startDestination = startDestination) {
                composable("activation") {
                    ActivationScreen(
                        onActivationSuccess = {
                            navController.navigate("drug_list") {
                                popUpTo("activation") { inclusive = true }
                            }
                        }
                    )
                }

                composable("drug_list") {
                    DrugListScreen(
                        onDrugSelected = { drugName, isContinuous, doseUnit, maxDose, minDose, isNeonate ->
                            val encodedName = java.net.URLEncoder.encode(drugName, "UTF-8")
                            navController.navigate(
                                "calculator/$encodedName/$isContinuous/$doseUnit/$maxDose/$minDose/$isNeonate"
                            )
                        }
                    )
                }

                composable(
                    "calculator/{drugName}/{isContinuous}/{doseUnit}/{maxDose}/{minDose}/{isNeonate}",
                    arguments = listOf(
                        navArgument("drugName") { type = NavType.StringType },
                        navArgument("isContinuous") { type = NavType.BoolType },
                        navArgument("doseUnit") { type = NavType.StringType },
                        navArgument("maxDose") { type = NavType.StringType },
                        navArgument("minDose") { type = NavType.StringType },
                        navArgument("isNeonate") { type = NavType.BoolType }
                    )
                ) { backStackEntry ->
                    val drugName = java.net.URLDecoder.decode(
                        backStackEntry.arguments?.getString("drugName") ?: "", "UTF-8"
                    )
                    val isContinuous = backStackEntry.arguments?.getBoolean("isContinuous") ?: false
                    val doseUnit = backStackEntry.arguments?.getString("doseUnit") ?: ""
                    val maxDose = backStackEntry.arguments?.getString("maxDose")?.toDoubleOrNull()
                    val minDose = backStackEntry.arguments?.getString("minDose")?.toDoubleOrNull()
                    val isNeonate = backStackEntry.arguments?.getBoolean("isNeonate") ?: false

                    CalculatorScreen(
                        drugName = drugName,
                        isContinuous = isContinuous,
                        doseUnit = doseUnit,
                        maxDose = maxDose,
                        minDose = minDose,
                        isNeonate = isNeonate,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}