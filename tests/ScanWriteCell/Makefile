# Project: ScanWriteCell
# Author: Erik Anderson
# Date: 16/10/2019

###########################################################
## Specify top modules
###########################################################
TOP=ScanWriteCell_tb

###########################################################
## Targets 
###########################################################
.PHONY: default
default:
	mkdir -p build/
	iverilog -g2012 -f file_list.txt -o build/$(TOP).out -t vvp -s $(TOP)
	vvp build/$(TOP).out -l build/vvp.log
	mv waves.vcd build/

.PHONY: view
view:
	gtkwave build/waves.vcd &

.PHONY: clean 
clean:
	rm -rf build/
