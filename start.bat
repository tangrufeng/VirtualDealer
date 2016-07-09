@ECHO OFF
@ECHO STARTUP App
@echo 当前目录 %cd%
@ECHO 设置环境变量,循环当前目录下的lib目录下所有jar文件,并设置CLASSPATH
FOR %%F IN (%cd%\lib\*.jar) DO call :addcp %%F
goto extlibe
:addcp
SET CLASSPATH=%CLASSPATH%;%1
goto :eof
:extlibe
@ECHO 显示CLASSPATH
SET CLASSPATH 
@ECHO 运行应用程序
java -cp %CLASSPATH%;vd1.0.jar org.tom.vd.Main
