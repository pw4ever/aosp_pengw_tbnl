# this file should be sourced in lunched-shell

obj_path=$(gettop)/out/target/common/obj
cp -v ${obj_path}/JAVA_LIBRARIES/framework_intermediates/classes.jar framework.jar
cp -v ${obj_path}/JAVA_LIBRARIES/core_intermediates/classes.jar core.jar
#cp -v ${obj_path}/APPS/framework-res_intermediates/package-export.apk framework-res.apk
