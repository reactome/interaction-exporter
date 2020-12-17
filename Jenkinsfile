// This Jenkinsfile is used by Jenkins to run the 'InteractionExporter' step of Reactome's release.
// It requires that the 'DiagramConverter' step has been run successfully before it can be run.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline{
	agent any
	// Set output folder where files generated by step will be stored.
	environment {
        	OUTPUT_FOLDER = "interactors"
    	}

	stages{
		// This stage checks that upstream project 'DiagramConverter' was run successfully.
		stage('Check DiagramConverter build succeeded'){
			steps{
				script{
                    			utils.checkUpstreamBuildsSucceeded("File-Generation/job/DiagramConverter/")
				}
			}
		}
		// This stage builds the jar file using maven.
		stage('Setup: Build jar file'){
			steps{
				script{
					sh "mvn clean package"
				}
			}
		}
		// Execute the jar file, producing interactions files for Human interactions and for all species interactions.
		stage('Main: Run Interaction-Exporter'){
			steps{
				script{
					sh "mkdir -p ${env.OUTPUT_FOLDER}"
					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						// Default behaviour is to run interactions on Human data
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/interaction-exporter-jar-with-dependencies.jar --user $user --password $pass --output ./${env.OUTPUT_FOLDER}/reactome.homo_sapiens.interactions --verbose"
						// Specify to generate interactions data for all species
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/interaction-exporter-jar-with-dependencies.jar --user $user --password $pass --output ./${env.OUTPUT_FOLDER}/reactome.all_species.interactions --species ALL --verbose"
					}
				}
			}
		}
		// Entire folder must be copied to the download folder.
		stage('Post: Copy interactors folder to download folder'){
		    steps{
		        script{
		            def releaseVersion = utils.getReleaseVersion()
		            sh "cp -r ${env.OUTPUT_FOLDER}/ ${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/"
		        }
		    }
		}
		// Archive everything on S3.
		stage('Post: Archive Outputs'){
			steps{
				script{
					def dataFiles = ["${env.OUTPUT_FOLDER}/*"]
					def logFiles = []
					def foldersToDelete = []
					utils.cleanUpAndArchiveBuildFiles("interactions_exporter", dataFiles, logFiles, foldersToDelete)
				}
			}
		}
	}
}