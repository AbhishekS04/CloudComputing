# Day 5: CloudSim Plus Scheduling

Day 5 introduces cloud-computing simulation with **CloudSim Plus**. The
practical creates one datacenter, two virtual machines, and four cloudlets,
then compares VM-level and cloudlet-level scheduling policies.

## Objectives

- Configure a Maven project with the CloudSim Plus dependency.
- Model a datacenter with one host and four processing elements.
- Create two VMs and four cloudlets.
- Compare space-shared and time-shared scheduling.
- Report each cloudlet's VM, status, start time, and finish time.

## Tools and environment

- Java 17
- Apache Maven
- CloudSim Plus 8.5.5
- Package: `lab`
- Main class: `lab.SchedulingExperiment`

The Maven dependency is declared in the root [`pom.xml`](../pom.xml).
The experiment implementation is in
[`SchedulingExperiment.java`](../src/main/java/lab/SchedulingExperiment.java).

## Simulation model

The experiment creates:

- One datacenter with one host.
- Four host processing elements, each rated at 1000 MIPS.
- Two VMs, each with one processing element, 2048 MB RAM, 10000 bandwidth,
  and a 10000-size disk.
- Four cloudlets, each with length 20000 and one processing element.
- Full CPU utilization and zero dynamic RAM/bandwidth utilization.

Each scenario creates a fresh `CloudSimPlus` simulation and prints a table of
completed cloudlets. The final count confirms how many of the four cloudlets
finished.

## Scheduling scenarios

The program compares these combinations:

| Scenario | VM scheduler | Cloudlet scheduler |
|---|---|---|
| A | Space-shared | Space-shared |
| B | Space-shared | Time-shared |
| C | Time-shared | Space-shared |
| D | Time-shared | Time-shared |

VM scheduling controls how host processing elements are allocated to VMs.
Cloudlet scheduling controls how each VM shares its processing capacity among
the cloudlets assigned to it.

## Run the experiment

From the repository root:

```bash
mvn compile
mvn exec:java -Dexec.mainClass=lab.SchedulingExperiment
```

If the Maven Exec plugin is not configured in the local environment, compile
with Maven and run the generated class using the project's normal Java
execution workflow.

The output contains one section per scenario with the columns:

```text
Cloudlet  VM  Status  Start  Finish
```

## Screenshot index

The screenshots record the Day 5 setup, implementation, compilation, and
simulation output:

- [1.0.png](1.0.png)
- [1.1.png](1.1.png)
- [2.png](2.png)
- [3.png](3.png)
- [4.png](4.png)
- [5.png](5.png)
- [6.png](6.png)
- [7.png](7.png)
- [8.png](8.png)
- [9.png](9.png)
- [10.png](10.png)
- [11.png](11.png)
- [12.png](12.png)

## Result

The practical demonstrates how CloudSim Plus can model datacenter resources,
VMs, cloudlets, and scheduling policies. Running the four scenarios makes the
effect of space-shared and time-shared allocation visible through cloudlet
placement and completion times.
