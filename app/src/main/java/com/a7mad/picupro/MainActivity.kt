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
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isActivated = ActivationManager.isActivated(this)

        setContent {
            val navController = rememberNavController()
            val startDestination = if (isActivated) "drug_list" else "activation"

            NavHost(navController = navController, startDestination = startDestination) {
                // شاشة التفعيل
                composable("activation") {
                    ActivationScreen(
                        onActivationSuccess = {
                            navController.navigate("drug_list") {
                                popUpTo("activation") { inclusive = true }
                            }
                        }
                    )
                }

                // شاشة قائمة الأدوية
                composable("drug_list") {
                    DrugListScreen(
                        onDrugSelected = { drugName ->
                            val encodedName = URLEncoder.encode(drugName, "UTF-8")
                            navController.navigate("drug_detail/$encodedName")
                        }
                    )
                }

                // شاشة تفاصيل الدواء
                composable(
                    "drug_detail/{drugName}",
                    arguments = listOf(
                        navArgument("drugName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val encodedName = backStackEntry.arguments?.getString("drugName") ?: ""
                    val drugName = URLDecoder.decode(encodedName, "UTF-8")

                    DrugDetailScreen(
                        drugName = drugName,
                        onBackClick = { navController.popBackStack() },
                        onProtocolSelected = { name, isContinuous, doseUnit, maxDose, minDose, isNeonate, route ->
                            val encodedName2 = URLEncoder.encode(name, "UTF-8")
                            val maxDoseStr = maxDose?.toString() ?: "null"
                            val minDoseStr = minDose?.toString() ?: "null"
                            val encodedRoute = URLEncoder.encode(route, "UTF-8")

                            navController.navigate(
                                "calculator/$encodedName2/$isContinuous/$doseUnit/$maxDoseStr/$minDoseStr/$isNeonate/$encodedRoute"
                            )
                        }
                    )
                }

                // شاشة الحاسبة
                composable(
                    "calculator/{drugName}/{isContinuous}/{doseUnit}/{maxDose}/{minDose}/{isNeonate}/{route}",
                    arguments = listOf(
                        navArgument("drugName") { type = NavType.StringType },
                        navArgument("isContinuous") { type = NavType.BoolType },
                        navArgument("doseUnit") { type = NavType.StringType },
                        navArgument("maxDose") { type = NavType.StringType },
                        navArgument("minDose") { type = NavType.StringType },
                        navArgument("isNeonate") { type = NavType.BoolType },
                        navArgument("route") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val drugName = URLDecoder.decode(
                        backStackEntry.arguments?.getString("drugName") ?: "", "UTF-8"
                    )
                    val isContinuous = backStackEntry.arguments?.getBoolean("isContinuous") ?: false
                    val doseUnit = backStackEntry.arguments?.getString("doseUnit") ?: ""
                    val maxDoseStr = backStackEntry.arguments?.getString("maxDose") ?: "null"
                    val minDoseStr = backStackEntry.arguments?.getString("minDose") ?: "null"
                    val isNeonate = backStackEntry.arguments?.getBoolean("isNeonate") ?: false

                    val maxDose = if (maxDoseStr == "null") null else maxDoseStr.toDoubleOrNull()
                    val minDose = if (minDoseStr == "null") null else minDoseStr.toDoubleOrNull()

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