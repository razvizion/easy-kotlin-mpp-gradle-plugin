package com.soywiz.korlibs.targets

import com.moowork.gradle.node.*
import com.soywiz.korlibs.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

fun Project.configureTargetJavaScript() {
	(project.extensions.getByName("node") as NodeExtension).apply {
		version = "12.12.0"
		//version = "10.16.3"
	}
    gkotlin.apply {
        js {
			this.attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
            compilations.all {
                it.kotlinOptions.apply {
                    languageVersion = "1.3"
                    sourceMap = true
                    metaInfo = true
                    moduleKind = "umd"
					suppressWarnings = korlibs.supressWarnings
                }
            }
            mavenPublication(Action { publication ->
                //println("JS publication: $publication : ${publication.name}")
            })
            browser {
                testTask {
                    //useMocha() // @TODO: Seems to produce problems where the JS file is produced and consumed
                }
            }
            nodejs {
                testTask {
                    //useMocha() // @TODO: Seems to produce problems where the JS file is produced and consumed
                }
            }
        }
    }

	afterEvaluate {
		for (target in korlibs.JS_TARGETS) {
			val taskName = "copyResourcesToExecutable_$target"
			val targetTestTask = tasks.findByName("${target}Test") as? Kotlin2JsCompile? ?: continue
			val compileTestTask = tasks.findByName("compileTestKotlin${target.capitalize()}") ?: continue
			val compileMainTask = tasks.findByName("compileKotlin${target.capitalize()}") ?: continue

			tasks {
				create<Copy>(taskName) {
					for (sourceSet in gkotlin.sourceSets) {
						from(sourceSet.resources)
					}

					into(targetTestTask.outputFile.parentFile.parentFile)
				}
			}

			targetTestTask.inputs.files(
				*compileTestTask.outputs.files.files.toTypedArray(),
				*compileMainTask.outputs.files.files.toTypedArray()
			)

			targetTestTask.dependsOn(taskName)
		}
	}


	dependencies.apply {
        add("jsMainImplementation", "org.jetbrains.kotlin:kotlin-stdlib-js")
        add("jsTestImplementation", "org.jetbrains.kotlin:kotlin-test-js")
    }
}

