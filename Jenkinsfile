def mvnCmd = "mvn "
def templatePath= "cicd/template.json"
pipeline {

		environment { 
		    NAME='users-microservice'
		    DEV_NAMESPACE='sample-app'
    	}

    	agent {
			label 'maven'
        }

        stages {
             stage('Checkout  proj') {
        steps {
                git branch: 'master',
                
                url: 'https://github.com/shivangi17jan/users-microservice.git'

                sh "ls -lat"
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


			stage('Code Analysis') {
                steps {
                  script {
                    sh "${mvnCmd} sonar:sonar -Dsonar.projectKey=users-microservice -Dsonar.host.url=https://sonar-sonar-qube-test.apps.aw-ue1-sb1.7v1h.p1.openshiftapps.com -Dsonar.login=sqp_58208e9e3b194d0a788102f6f7abc181dd3bfd4c -Dversion=${env.BUILD_NUMBER}"
                  }
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

					
	    	 }     
            } 
       } 
