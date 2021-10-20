# flakiness_replayer

The execution of the script below will download an example project from github (fastjson) and instrument an arbitrary method, adding delays to that test method (sleeps). 

1. Instrument the code

```
$> bash demo.sh
```

2. Check instrumented class was generated

```
$> find sootOutput
```

TODO: Execute the test method (need to add `sootOutput` in the classpath)
