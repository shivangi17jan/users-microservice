def mvnCmd = "mvn -s configuration/settings.xml"
def templatePath= "cicd/template.json"
pipeline {

		environment { 
		    NAME='camel-springboot-xml'
		    DEV_NAMESPACE='dev-esb'
		    QA_NAMESPACE='qa-esb'
		    PROD_NAMESPACE='prod-esb'
    	}

    	agent {
			label 'maven'
        }

        stages {
            stage('Checkout') {
		      when {
			    expression {
				    env.PROJECT == env.DEV_NAMESPACE
			     }     
			   }

				steps {
                	checkout scm
		    	}
             }

            stage('Code Compile') {
		       when {
			      expression {
				      env.PROJECT == env.DEV_NAMESPACE
			      } 
				}
                steps {
                  sh "${mvnCmd} clean package -DskipTests=true -Dversion=${env.BUILD_NUMBER}"
                }
            }

			stage('Validate Routes') {
                steps {
				  sh " ${mvnCmd} camel:validate -DskipTests=true -Dversion=${env.BUILD_NUMBER}"
                }
              }

			stage('Camel Report Maven Plugin') {
                steps {
				  sh " ${mvnCmd} camel:route-coverage -DskipTests=true -Dversion=${env.BUILD_NUMBER}"
                }
              }

			stage('Execute Test Cases'){
						steps{
								sh "${mvnCmd} test -Dversion=${env.BUILD_NUMBER}"
								junit "target/surefire-reports/*.xml"
						}
			}

			stage('Code Analysis') {
                steps {
                  script {
                    sh "${mvnCmd} sonar:sonar -Dsonar.host.url=http://sonarqube:9000  -Dsonar.login=752afa04d03703e6bf113bbe5ecaf36dd836e1f6 -DskipTests=true -Dversion=${env.BUILD_NUMBER}"
                  }
                }
              }


			
            stage('Upload artifact to nexus') {
		       when {
			      expression {
				     env.PROJECT == env.DEV_NAMESPACE
			      } 
				}
                steps {
                  sh "${mvnCmd} deploy -DskipTests=true -Dversion=${env.BUILD_NUMBER}"
                }
              } 

             stage ('Deploy Template') {
		      steps{
			     script{
					try {
				    openshift.withCluster() {
					openshift.withProject(env.PROJECT) {
						echo "Using project: ${openshift.project()}"
						if(!openshift.selector("all", [ template : "${NAME}"]).exists()){
							openshift.newApp(templatePath,"-p PROJECT=${env.PROJECT}")

						} 
						if (env.PROJECT == "${QA_NAMESPACE}"){
							openshift.tag("${DEV_NAMESPACE}/${NAME}:latest", "${PROJECT}/${NAME}:latest")	
						}
						if (env.PROJECT == "${PROD_NAMESPACE}"){
							openshift.tag("${QA_NAMESPACE}/${NAME}:latest", "${PROJECT}/${NAME}:latest")	
						}
					} 
				  } 
			    }
				catch ( e ) {
					   	    echo e.getMessage()
		        		   	error "Deploy Template not successful."
		    		       }   
		  		 } 
				    } 
					   
			 }

	            stage('Image build') { 
		        when {
			       expression {
				      env.PROJECT == env.DEV_NAMESPACE
			      } 
					}
				  steps{      
					  script{
					    try { 	  
			               	    timeout(time: 5, unit: 'MINUTES') {	  
								openshift.withCluster() {
							    openshift.withProject(env.PROJECT) {
								echo "Using project in Image Build ${openshift.project()}  ${NAME}-${env.BUILD_NUMBER}.jar"
								def build = openshift.selector("bc", "${NAME}").startBuild("--from-file=target/${NAME}-${env.BUILD_NUMBER}.jar", "--wait=true")
								build.untilEach {
									echo "Using project in Image Build ${build}"
									return it.object().status.phase == "Complete"
								} 
						     } 
					         } 
				             }	
				             echo "STAGE Image Build Template Finished" 	  
		                     } 
					   catch ( e ) {
					   	    echo e.getMessage()
		        		   	error "Build not successful."
		    		       } 
				     } 
				  }  
			       } 

			        stage('Deployment') {     	 
					     steps{ 
						  script{     
							 try {  
								timeout(time: 5, unit: 'MINUTES') {	
							     openshift.withCluster() {
								openshift.withProject(env.PROJECT) {

								 	echo "Using project: ${openshift.project()}"
									if (openshift.selector("cm", "${NAME}").exists()){
									    openshift.selector("cm", "${NAME}").delete();
									}
									openshift.create("cm","${NAME}","--from-file=src/main/resources/application-${PROJECT}.properties")
								   def deploy= openshift.selector("dc", "${NAME}")
							           deploy.rollout().latest();
								  } 
						                  }   		
					               		    } 
						    	       } 
							       catch ( e ) {
								error "Deployment not successful."
			    		                     } 
						  } 	  
					      }	
					 } 

					 stage('Image Tag') {
	           steps {
			script {
		 	      openshift.withCluster() {
				      openshift.tag("${PROJECT}/${NAME}:latest", "${PROJECT}/${NAME}:${env.BUILD_NUMBER}")
		  	     }
			}
	      	    }
	    	 }     
            } 
       } 
