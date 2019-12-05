Following are the steps need to be performed to enable ssl mutual authentication in APS.
1) Place the shared APS-SSLMutualAuth-1.0.jar,keystore.properties,whitelisted-domains.conf files in <APS_Install_Dir>/tomcat/webapps/activiti-app/WEB-INF/lib.
2) Provide the list of domains in the whitelisted-domains.conf file. e.g., https://<ipaddress>:<port>.
3) Provide the values for 'keystore_path' and 'keystore_password' in the keystore.properties file.
4) Restart APS.
