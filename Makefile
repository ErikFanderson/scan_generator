# Project: mems_switch_driver
# Author: Erik Anderson
# Date: 10/08/2019

pkg=mems_switch_control

.PHONY: default 
default: run

.PHONY: run 
run:
	mill $(pkg).run
	mkdir -p generated/
	mv *.f *.fir *.v *.anno.json generated/

.PHONY: compile
compile:
	mill $(pkg).compile

.PHONY: test 
test:
	mill $(pkg).test

.PHONY: clean
clean:
	mill clean
	rm -rf *.anno.json *.fir *.v generated/ out/ test_run_dir/
