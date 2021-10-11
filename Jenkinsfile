def readProb;
def FAILED_STAGE
pipeline {
agent { label 'master'}


tools {
  maven 'maven-3'
  git 'Default'
}
stages {
    stage('Preperation'){
    steps {
        script {
        readProb = readProperties  file:'config.properties'
        FAILED_STAGE=env.STAGE_NAME
        Preperation= "${readProb['Preperation']}"
		if ("$Preperation" == "yes") {
	    sh "git config --global user.email admin@example.com"
        sh "git config --global user.name Administrator"
        sh 'git config --global credential.helper cache'
        sh 'git config --global credential.helper cache'
        sh "if [ -d ${readProb['Project_name']} ]; then rm -Rf ${readProb['Project_name']}; fi"
        sh "if [ -d ${readProb['PMD_result']} ]; then rm -Rf ${readProb['PMD_result']};  fi"        
        sh "mkdir ${readProb['PMD_result']}"
		}
		else {
		 echo "Skipped"
		}
		}
		}
    }
    stage('Git Pull'){
    steps {	dir("${readProb['Project_name']}"){
	    git branch: "${readProb['branch']}", credentialsId: "${readProb['credentials']}", url: "${readProb['Bitbucket.url']}"    
	      }
		}
	 }
	stage("Validate NOPMD Usage") {
	steps {
        script {
        FAILED_STAGE=env.STAGE_NAME
        NOPMD= "${readProb['NOPMD']}"
		if ("$NOPMD" == "yes") {
	    sh (
	       script: '''
	    cd demo
	    echo $PWD
        nosonar="$(grep -ir nosonar  | wc -l)"
        nopmd="$(grep -ir nopmd  | wc -l)"
        nosquid="$(grep -ir squid  | wc -l)"
        if [ "${nosonar}" == 0 ] && [ "${nopmd}" == 0 ] && [ "${nosquid}" == 0 ]; then
        exit 1
        else
        grep -ir nopmd    > ${WORKSPACE}/${readProb['PMD_result']}/nopmd.report
        grep -ir nosonar  > ${WORKSPACE}/${readProb['PMD_result']}/nosonar.report
        grep -ir squid    > ${WORKSPACE}/${readProb['PMD_result']}/nosquid.report 
        echo "No Sonar was used ${nosonar} times in the code" 
        echo "No PMD was used ${nopmd} times in the code"
        echo "squid supress was used ${nosquid} times in the code"
        exit 0
        fi '''
        )
		}
		else {
		  echo "skipped"
		 }
	    }
	   }
	 }
	stage('ClamAV') {
	 parallel {
	 stage('Scan') {
	  steps {
	    script {
        FAILED_STAGE=env.STAGE_NAME
		 ClamAV_scan= "${readProb['ClamAV']}"
		    if ("$ClamAV_scan" == "yes"){
       build job: 'demo_clamav', wait: false
       } else
     echo "Skipped"
	     }
	    }
	   }
	  }
    }
    stage ('Build Stage') {
    steps {
            sh 'mvn -f $WORKSPACE/pom.xml clean install'
            }
    }
    
    stage('upload') {
       steps {
          script { 
             def server = Artifactory.server 'artifactory'
             def uploadSpec = """{
                "files": [{
                   "pattern": "${WORKSPACE}/target/newapp-0.0.1-SNAPSHOT.war",
                   "target": "example-repo-local"
                }]
              }"""
              server.upload(uploadSpec) 
              }
            }
        }
    stage('SonarQube analysis') {
	  steps {
	    script {
         scannerHome = tool 'sonarqube';
	     FAILED_STAGE=env.STAGE_NAME
	   	  SonarQube= "${readProb['SonarQube_Analysis']}"
		if ("$SonarQube" == "yes") {
          withSonarQubeEnv('sonarqube') {
          sh "${scannerHome}/bin/sonar-scanner -X -Dsonar.login=admin -Dsonar.password=kodewatch -Dsonar.projectKey=demo -Dsonar.projectName=demo -Dsonar.projectVersion=1.0  -Dsonar.sources=${readProb['sonar_sources']} -Dsonar.java.sourceEncoding=ISO-8859-1 -Dsonar.java.binaries=${readProb['sonar_java_binaries']}"
           }
	    }
		else {
		  echo "Skipped"
		  }
		 }
		}
     }
    stage("Sonarqube Quality Gate") {
	   steps {
	    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') { script {  
            FAILED_STAGE=env.STAGE_NAME
			Quality= "${readProb['SonarQube_Quality']}"
		    if ("$Quality" == "yes") {
            sleep(60)
            qg = waitForQualityGate() 
            if (qg.status != 'OK') {
            error "Pipeline aborted due to quality gate failure: ${qg.status}"	
              }
            }
			else {
			echo "skipped"
			}
		   }
         }
     }
    stage('Docker Build') {
      agent any
      steps {
        sh 'docker build -t jfrog-cicd.kodewatch.com/docker/cicd-dockerimage:$BUILD_NUMBER  /var/jenkins_home/workspace/demo/.'
      }
    }
	
    stage('Dev Anchore') {    
        steps {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') { script {
         FAILED_STAGE=env.STAGE_NAME
         anchore= "${readProb['Dev_anchore']}"
		 if ("$anchore" == "yes") {
		 script {
		  sh 'rm -rf anchore_images || true'
		  sh 'echo "jfrog-cicd.kodewatch.com/docker/cicd-dockerimage:$BUILD_NUMBER $WORKSPACE/Dockerfile" > anchore_images'
          anchore bailOnPluginFail: false, name: 'anchore_images'
			  }
			}
		 else {
		 echo "Skipped"
		       }
		       }
		      }
       	     }
	       }
	
    stage('Docker Push') {
      agent any
      steps {        
          sh 'echo script done'
          sh 'docker login -u admin -p kodewatch jfrog-cicd.kodewatch.com'
          sh 'docker push jfrog-cicd.kodewatch.com/docker/cicd-dockerimage:$BUILD_NUMBER'        
      }
    }
        stage("Dev Deploy") {
           steps {
           script {
            FAILED_STAGE=env.STAGE_NAME
                  Dev_deploy= "${readProb['Dev_Deploy']}"
                    if ("$Dev_deploy" == "yes") {
	          sh 'cp /opt/script/docker-login.sh /var/jenkins_home/jobs/demo/'
	          sh 'cp /opt/script/docker-logout.sh /var/jenkins_home/jobs/demo/'
                  sh 'bash /var/jenkins_home/jobs/demo/docker-login.sh'         
                  sh '''sed  -i "s/kodewatch01\\/cicd-dockerimage/jfrog-cicd.kodewatch.com\\/docker\\/cicd-dockerimage/g"  /var/jenkins_home/workspace/demo/deploy.yml
                  echo  $BUILD_NUMBER                  
                  sed -i s/latest/$BUILD_NUMBER/g /var/jenkins_home/workspace/demo/deploy.yml
                  kubectl apply -f /var/jenkins_home/workspace/demo/deploy.yml'''
                  sh 'sleep 55s'
                  sh 'ip=$(kubectl get svc | grep tomcat | tr -s [:space:] \' \' | cut -d \' \' -f 4) && echo https://kodewatch:kodewatch@$ip:8080/newapp-0.0.1-SNAPSHOT/ && echo http://kodewatch:kodewatch@k8sdeploy-cicd.kodewatch.com/newapp-0.0.1-SNAPSHOT'		
                  }
                  else {
                  echo "Skipped"
                  }
            }
           }
          } 
	 stage('Dev VAPT') {
         agent any
         steps {
         script {
         FAILED_STAGE=env.STAGE_NAME
         vapt= "${readProb['Dev_vapt']}"
                 if ("$vapt" == "yes") {
       sh '''
                  echo "openvas server execution"
		  mkdir -p /var/jenkins_home/workspace/demo/openvas
                  if ls /var/jenkins_home/workspace/demo/openvas/*.pdf > /dev/null 2>&1; then
                  rm -rf /var/jenkins_home/workspace/demo/openvas/*.pdf
		  cp /opt/script/kodewatch1.py /var/jenkins_home/workspace/demo/openvas
                  chmod 777 /var/jenkins_home/workspace/demo/openvas/kodewatch1.py
                  cd /var/jenkins_home/workspace/demo/openvas
                  ip="$(kubectl get svc | grep complete-cicd | tr -s [:space:] \' \' | cut -d \' \' -f 4)" && ./kodewatch1.py $ip
                  else
		  rm -rf /var/jenkins_home/workspace/demo/openvas/*
		  cp /opt/script/kodewatch.py /var/jenkins_home/workspace/demo/openvas
                  chmod 777 /var/jenkins_home/workspace/demo/openvas/kodewatch.py
                  cd /var/jenkins_home/workspace/demo/openvas
                  ip="$(kubectl get svc | grep complete-cicd | tr -s [:space:] \' \' | cut -d \' \' -f 4)" && ./kodewatch.py $ip
                  fi
          '''	
                  sh 'curl -u admin:kodewatch -X DELETE "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/vapt-report" || true'
                  sh 'curl -u admin:kodewatch -X MKCOL "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/vapt-report"'
	          sh 'curl -u admin:kodewatch -X PUT "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/vapt-report/Report_for_openvas.pdf" -F myfile=@"/var/jenkins_home/workspace/demo/openvas/Report_for_openvas.pdf"'


                        }
		}
	}
}


      stage("ZAProxy") {
           steps {
             script {
                 sh 'bash /var/jenkins_home/jobs/demo/docker-logout.sh'		 
                 sh '''job=$(kubectl get job | grep zaproxy-job | wc -l)
                 if [ $job -eq 1 ]; then
                   kubectl delete job zaproxy-job 
                   ip=$(kubectl get svc | grep tomcat | tr -s [:space:] \' \' | cut -d \' \' -f 4) && sed -i "s/http:\\/\\/15.206.11.209/http:\\/\\/kodewatch:kodewatch\\@$ip:8080\\/newapp-0.0.1-SNAPSHOT\\//g" /var/jenkins_home/workspace/demo/zaproxy-job.yaml
                   kubectl apply -f  /var/jenkins_home/workspace/demo/zaproxy-job.yaml
                 else
                   ip=$(kubectl get svc | grep tomcat | tr -s [:space:] \' \' | cut -d \' \' -f 4) && sed -i "s/http:\\/\\/15.206.11.209/http:\\/\\/kodewatch:kodewatch\\@$ip:8080\\/newapp-0.0.1-SNAPSHOT\\//g" /var/jenkins_home/workspace/demo/zaproxy-job.yaml
                   kubectl apply -f  /var/jenkins_home/workspace/demo/zaproxy-job.yaml    
                 fi'''
             }
           }
       }
        stage("sitespeed") {
           steps {
             script {
                 sh '''job=$(kubectl get job | grep sitespeed-job | wc -l)
                 if [ $job -eq 1 ]; then
                    kubectl delete job sitespeed-job 
                    sleep 1m 
                    ip=$(kubectl get svc | grep tomcat | tr -s [:space:] \' \' | cut -d \' \' -f 4) && sed -i "s/http:\\/\\/15.206.11.209/http:\\/\\/kodewatch:kodewatch\\@$ip:8080\\/newapp-0.0.1-SNAPSHOT\\//g" /var/jenkins_home/workspace/demo/sitespeed-job.yaml
                    kubectl apply -f  /var/jenkins_home/workspace/demo/sitespeed-job.yaml
                else
                    ip=$(kubectl get svc | grep tomcat | tr -s [:space:] \' \' | cut -d \' \' -f 4) && sed -i "s/http:\\/\\/15.206.11.209/http:\\/\\/kodewatch:kodewatch\\@$ip:8080\\/newapp-0.0.1-SNAPSHOT\\//g" /var/jenkins_home/workspace/demo/sitespeed-job.yaml
                    kubectl apply -f  /var/jenkins_home/workspace/demo/sitespeed-job.yaml    
                fi'''
             }
           }
       }
      stage("ZAProxy_report") {
           steps{
             script {
               sh 'sleep 10m'
               sh 'rm -rf /var/jenkins_home/jobs/demo/builds/archive/out/*'
               sh 'curl -u admin:kodewatch -X GET "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/zaproxy-report/demo_Dev_ZAP_VULNERABILITY_REPORT.html" -o /var/jenkins_home/jobs/demo/builds/archive/out/demo_Dev_ZAP_VULNERABILITY_REPORT.html'
               sh 'curl -u admin:kodewatch -X GET "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/zaproxy-report/demo_Dev_ZAP_VULNERABILITY_REPORT.xml" -o /var/jenkins_home/jobs/demo/builds/archive/out/demo_Dev_ZAP_VULNERABILITY_REPORT.xml'
             }
           }
      }
      stage("sitespeed_report") {
           steps{
             script {
	       sh 'curl -u admin:kodewatch -X GET "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/sitespeed-report/sitespeed.html" -o /var/jenkins_home/jobs/demo/builds/archive/out/sitespeed.html'
             }
           }
      }
      stage("dependencycheck_report") {
           steps{
             script {
	       sh "echo $NS"
               sh 'rm  -rf /var/jenkins_home/jobs/demo/builds/archive/out/dependency-check-report.xml'
               sh 'curl -u admin:kodewatch -X GET "http://owncloud.$NS.svc.cluster.local:8080/remote.php/webdav/Documents/dependency-check-report.xml" -o /var/jenkins_home/jobs/demo/builds/archive/out/dependency-check-report.xml'

             }
           }
      }
      stage ('Selenium TestNG Report Stage') {
         steps {
                step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])
               }
      }


    }

  post {
      success {
            publishHTML target: [
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '/var/jenkins_home/jobs/demo/builds/archive/out',
            reportFiles: 'demo_Dev_ZAP_VULNERABILITY_REPORT.html',
            reportName: 'Dev_ZAP_VULNERABILITY_REPORT'
              ]
	    publishHTML target: [
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '/var/jenkins_home/jobs/demo/builds/archive/out',
            reportFiles: 'dependency-check-report.xml',
            reportName: 'Dependency-check'
              ]
            publishHTML target: [
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '/var/jenkins_home/jobs/demo/builds/archive/out',
            reportFiles: 'sitespeed.html',
            reportName: 'Dev_speedtest'
              ]
            publishHTML target: [
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '/var/jenkins_home/workspace/demo/TestReport',
            reportFiles: 'TestReport.html',
            reportName: 'Dev_Testng_Report'
              ]
            }
        }

}
