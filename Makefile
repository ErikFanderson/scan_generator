# Project: mems_switch_driver
# Author: Erik Anderson
# Date: 10/08/2019

module=scan_generator
test_module=verilog_test
config=example.yml

.PHONY: default 
default: run

.PHONY: run 
run:
	./mill $(module).run $(config)
	mkdir -p generated/
	mv *.f *.fir *.v *.anno.json generated/

.PHONY: compile
compile:
	./mill $(module).compile

.PHONY: assembly 
assembly:
	./mill $(module).assembly

.PHONY: test 
test:
	./mill $(test_module).run
	mkdir -p generated/
	mv *.f *.fir *.v *.anno.json generated/

.PHONY: clean
clean:
	./mill clean
	rm -rf *.anno.json *.fir *.v generated/ out/ test_run_dir/
