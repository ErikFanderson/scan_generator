# Project: mems_switch_driver
# Author: Erik Anderson
# Date: 10/08/2019

module=scan_generator

.PHONY: default 
default: run

.PHONY: run 
run:
	mill $(module).run
	mkdir -p generated/
	mv *.f *.fir *.v *.anno.json generated/

.PHONY: compile
compile:
	mill $(module).compile

.PHONY: test 
test:
	mill $(module).test

.PHONY: clean
clean:
	mill clean
	rm -rf *.anno.json *.fir *.v generated/ out/ test_run_dir/
