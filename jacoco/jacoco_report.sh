#!/bin/sh

#repo 목록 jeus인스턴스명:jacoco tcpserver port
REPORT_HOME=/home/{사용자}/jacoco
JACOCO_HOME=/{설치경로}/jacoco-0.8.8
JAVA_HOME=/{설치경로}/java/openjdk-8u332

while true
do
        read -p ">>> coverage dump를 생성할 서버인스턴스 명을 널으세요 : " targetIns

		read -p ">>> coverage dump를 생성할 jacoco tcpserver port를 넣어주세요 : " jacocoPort

        if [ ! -z "${targetIns}" ] && [ ! -z "${jacocoPort}" ]
        then
			                    #업무별 jacoco 덤프 생성
                                jacocodump="echo '{비밀번호}' | sudo -kS /{설치경로}/java/openjdk-8u332/bin/java -jar /{설치경로}/jacoco-0.8.8/lib/jacococli.jar dump --address localhost --port ${jacocoPort} --destfile ${REPORT_HOME}/dump/${targetIns}/jacoco-it.exec"


                                echo ""
                                echo ""
                                sshpass -p {비밀번호} ssh {계정}@{서버아이피}  -o StrictHostKeyChecking=no ${jacocodump}

								mkdir -p ${REPORT_HOME}/${targetIns}
                                echo ""
                                echo ":: ${targetIns} - 원격서버 Jacoco Dump 생성 완료!!"
                                echo ""
                                #업무별 jacoco 덤프 가져오기
                                sshpass -p {비밀번호} scp -o StrictHostKeyChecking=no {계정}@{서버아이피}:/home/{사용자}/jacoco/dump/${targetIns}/jacoco-it.exec ${REPORT_HOME}/${targetIns}/

                                echo ""
                                echo ":: ${targetIns} - 원격서버 Jacoco Dump 복사 완료!!"
                                echo ""

                                read -p ">>> 젠킨스 프로젝트명을 넣어주세요 : " jenkinsName

                                while true
                                do
                                	read -p ">>> 소스  프로젝트명을 넣어주세요 : " pjtName
                                	classfiles="/{젠킨스경로}/workspace/${jenkinsName}/${pjtName}/target/classes/TEST"
                                    sourcefiles="/{젠킨스경로}/workspace/${jenkinsName}/${pjtName}/src/main/java"

									mkdir -p ${REPORT_HOME}/${targetIns}/${pjtName}

                                	#리포트생성
                                	echo ""
                                	echo ""
                                	echo '{비밀번호}' | sudo -kS ${JAVA_HOME}/bin/java -jar ${JACOCO_HOME}/lib/jacococli.jar report ${REPORT_HOME}/${targetIns}/jacoco-it.exec --classfiles ${classfiles} --sourcefiles ${sourcefiles} --html ${REPORT_HOME}/${targetIns}/${pjtName}/jacoco-report --csv  ${REPORT_HOME}/${targetIns}/${pjtName}/jacoco-report.csv --xml  ${REPORT_HOME}/${targetIns}/${pjtName}/jacoco-report.xml

                                	#ftp에서 다운로드 하기위해 권한변경
                                	echo '{비밀번호}' | sudo -kS chown -Rf {사용자}:{그룹} ${REPORT_HOME}/${targetIns}
                                	
                                	echo ""
                                	echo ""
                                	echo "================== 리포트 생성완료 =================="
                                	echo ":: 젠킨스 프로젝트 = ${jenkinsName}"
                                	echo ":: 소스 프로젝트  = ${pjtName}"
                                	echo ":: REPORT 경로 = ${REPORT_HOME}/${targetIns}/${pjtName}/"
                                	echo "================== 리포트 생성완료 =================="
									echo ""
									echo ""
                                	read -p ">>> 추가적으로 생성할 coverage report를 생성할 프로젝트가 있나요(Y/N) : " addYn
                                	if [ "Y" != "${addYn}" ]; then
                                		break 3
                                	fi
                                done
        fi
done
