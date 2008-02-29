

INSTALL_HOME=`pwd`
IZ_HOME=/opt/izpack

CMD="java -cp $IZ_HOME/lib/standalone-compiler.jar \
	-Dtools.jar=$JAVA_HOME/lib/tools.jar \
	-Dizpack.home=$IZ_HOME \
	com.izforge.izpack.compiler.Compiler \
	$INSTALL_HOME/izpack_install.xml -b \
	$INSTALL_HOME/../rte -o $INSTALL_HOME/setup "


echo $CMD

cd $IZ_HOME
$CMD

