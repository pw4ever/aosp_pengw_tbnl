# this script needs to be "source"d---for $(gettop) from AOSP envsetup.sh

managers=$(find $(gettop)/frameworks/base/core/java/ -name 'I*Manager.*' | perl -wnl -e 'if(/\/java\/(.*I[A-Z][^\/]+Manager)\..*$/) { $n=$1; $n =~ s{/}{.}g; print "$n";}' | sort)

for i in $managers; do
    echo "import $i;"
done

echo 

for i in $managers; do
    root=$(echo $i | perl -wnl -e 'print $1 if /\.I([^.]+)$/')
    echo "private I${root} m$root;"
    echo "private boolean m${root}Enabled = false;"
done

echo

for i in $managers; do
    root=$(echo $i | perl -wnl -e 'print $1 if /\.I([^.]+)$/')
    srv=$(echo $root | perl -wnl -e 's/Manager/Service/g; print join "_", map { uc $_ } split /(?=[A-Z])/, $_')

    echo "if (m${root}Enabled) {"
    echo "while ((m${root} = I${root}.Stub.asInterface(ServiceManager.getService(Context.${srv}))) == null) {"
    echo "System.err.println(\"obtaining I${root} handler...\");"
    echo "Thread.sleep(waitTime);"
    echo "}"
    echo "}"
    echo
    
done
