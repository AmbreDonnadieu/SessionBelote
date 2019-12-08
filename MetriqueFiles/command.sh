cd ~/Documents/UQAC/8INF957/SessionBelote/MetriqueFiles
mkdir -p ./resultat

FILE1=badclass
FILE2=classTooLenght
FILE3=complexity
FILE4=coupling
FILE5=cycloComplexity
FILE6=documentation
FILE7=programSizeInLinesOfCodes

METRICS=${FILE7}

OUT_SUFFIXES=

~/pmd-bin-6.18.0/bin/run.sh pmd -no-cache -t 2 -R ./xml/${METRICS}.xml -d ../JBelote/src/ -f text > ./resultat/${METRICS}${OUT_SUFFIXES}
