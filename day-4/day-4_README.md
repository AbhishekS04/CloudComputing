# Day 4: Software-Defined Networking (SDN)

This lab builds a small Software-Defined Networking environment on Ubuntu
using **Mininet**, **Open vSwitch (OVS)**, and the **Ryu SDN controller**.
Three virtual hosts connect to one OVS switch. Ryu controls the switch through
OpenFlow 1.3, and an OpenFlow DROP rule is used to demonstrate traffic
filtering.

## Objectives

- Prepare Ubuntu and install the SDN lab dependencies.
- Install and verify Mininet and Open vSwitch.
- Create an isolated Python environment for Ryu.
- Start the Ryu `simple_switch_13` application.
- Build a single-switch topology containing H1, H2, and H3.
- Verify host connectivity and inspect switch flow entries.
- Add a filtering rule, test its effect, and remove the flow entries.
- Clean up the Mininet environment.

## Tools and environment

- Ubuntu Linux virtual machine
- Mininet
- Open vSwitch
- Python 3 and `venv`
- Ryu SDN controller
- OpenFlow 1.3
- Ryu controller listening on TCP port `6653`

The Ryu virtual environment used for this lab was created with Python 3.10:

```bash
python3.10 -m venv ~/ryu-env
source ~/ryu-env/bin/activate
```

## Topology

```text
             Ryu Controller
             127.0.0.1:6653
                    |
             OpenFlow 1.3
                    |
                   S1
                /  |  \
              H1  H2  H3
```

## Procedure and commands

Run the commands in order. Commands marked **Mininet CLI** must be entered
at the `mininet>` prompt. Keep the Ryu controller terminal open while the
Mininet topology is running.

### 1. Update Ubuntu

```bash
sudo apt update && sudo apt upgrade -y
```

Updates package indexes and upgrades installed packages.

Screenshot: `01_ubuntu_update.png`

### 2. Install Mininet

```bash
sudo apt install mininet -y
```

Installs Mininet for creating emulated hosts, switches, and links.

Screenshot: `02_install_mininet.png`

### 3. Install Open vSwitch

```bash
sudo apt install openvswitch-switch -y
```

Installs the software switch used by the Mininet topology.

Screenshot: `03_install_ovs.png`

### 4. Verify the Open vSwitch service

```bash
sudo systemctl status openvswitch-switch
```

Confirm that the service reports `active (running)`. Press `q` to leave the
status viewer.

Screenshot: `04_ovs_status.png`

### 5. Test Mininet independently

```bash
sudo mn
```

At the `mininet>` prompt:

```text
nodes
pingall
exit
```

This checks that Mininet can start a basic network and that its default hosts
can communicate.

Screenshots:
- `05_mininet_start.png`
- `06_mininet_nodes.png`
- `07_mininet_pingall.png`

### 6. Install Python tooling

```bash
sudo apt install python3-pip python3-venv -y
```

Installs pip and Python virtual-environment support.

Screenshot: `08_python_venv_install.png`

### 7. Create the Ryu virtual environment

```bash
python3.10 -m venv ~/ryu-env
```

Creates a dedicated environment using Python 3.10, keeping Ryu's Python
packages separate from Ubuntu's system Python.

Screenshot: `09_create_ryu_env.png`

### 8. Activate the environment

```bash
source ~/ryu-env/bin/activate
```

The shell prompt should show `(ryu-env)`.

Screenshot: `10_activate_ryu_env.png`

### 9. Update Python packaging tools

```bash
python -m pip install --upgrade pip setuptools wheel
```

Updates the packaging tools inside the virtual environment before installing
Ryu.

Screenshot: `11_upgrade_pip.png`

### 10. Install Ryu

```bash
pip install ryu
```

Installs the Ryu controller and its Python dependencies in the virtual
environment.

Screenshot: `12_install_ryu.png`

### 11. Verify Ryu

```bash
ryu-manager --version
```

Displays the installed Ryu version.

Screenshot: `13_ryu_version.png`

### 12. Start the Ryu controller

In the first terminal, with `(ryu-env)` active:

```bash
ryu-manager --ofp-tcp-listen-port 6653 ryu.app.simple_switch_13
```

Leave this terminal running. The application provides basic Layer 2 learning
switch behavior using OpenFlow 1.3.

Screenshot: `14_ryu_controller.png`

### 13. Create the remote-controller topology

Open a second terminal and run:

```bash
sudo mn --topo single,3 --controller=remote,ip=127.0.0.1,port=6653 --switch ovsk,protocols=OpenFlow13
```

This creates three hosts (`h1`, `h2`, `h3`) connected to one OVS switch
(`s1`). The switch connects to the Ryu controller on the local VM at TCP
port `6653`.

Screenshot: `15_sdn_topology.png`

### 14. Inspect the nodes

**Mininet CLI:**

```text
nodes
```

Expected node names include `c0`, `h1`, `h2`, `h3`, and `s1`.

Screenshot: `16_sdn_nodes.png`

### 15. Inspect the links

**Mininet CLI:**

```text
net
```

Confirms the host-to-switch connections and interface names.

Screenshot: `17_network_connections.png`

### 16. Test all-host connectivity

**Mininet CLI:**

```text
pingall
```

A successful test should report no packet loss.

Screenshot: `18_pingall_success.png`

### 17. Test H1-to-H2 connectivity

**Mininet CLI:**

```text
h1 ping -c 4 h2
```

This sends four ICMP echo requests from H1 to H2. Before filtering, the
expected result is successful replies with no packet loss.

Screenshot: `19_h1_h2_ping.png`

### 18. Inspect OpenFlow flow entries

Open a third, ordinary Ubuntu terminal and run:

```bash
sudo ovs-ofctl -O OpenFlow13 dump-flows s1
```

Displays the OpenFlow 1.3 entries installed on switch `s1`. The output can
include learned forwarding entries and the controller rule.

Screenshot: `20_flow_rules.png`

### 19. Add a traffic-filtering rule

In the ordinary Ubuntu terminal:

```bash
sudo ovs-ofctl -O OpenFlow13 add-flow s1 "priority=100,in_port=1,actions=drop"
```

Adds a high-priority rule that drops packets entering switch port 1. In this
topology, port 1 is connected to H1.

Screenshot: `21_add_filter_rule.png`

### 20. Test the filtering rule

Return to the **Mininet CLI** and run:

```text
h1 ping -c 4 h2
```

The expected result is that the four packets are not received (typically
100% packet loss). This demonstrates that the DROP rule blocks H1's traffic.

Screenshot: `22_filtering_test.png`

### 21. Remove flow entries

In the ordinary Ubuntu terminal:

```bash
sudo ovs-ofctl -O OpenFlow13 del-flows s1
```

This deletes all flow entries from `s1`, including the manually added DROP
entry and the forwarding entries installed by Ryu. It is the broad deletion
command from the assignment. The switch may need to relearn forwarding entries
afterward; this lab proceeds to stop and clean up the topology.

Screenshot: `23_remove_flow_rules.png`

### 22. Exit Mininet

At the **Mininet CLI**:

```text
exit
```

Exits the Mininet command-line interface and stops the running topology.

Screenshot: `24_exit_mininet.png`

### 23. Clean Mininet

In an ordinary Ubuntu terminal:

```bash
sudo mn -c
```

Removes leftover Mininet processes, interfaces, and temporary network state.

Screenshot: `25_mininet_cleanup.png`

### 24. Final service check

```bash
sudo systemctl is-active openvswitch-switch
```

Expected output:

```text
active
```

This confirms that the Open vSwitch service remains active after Mininet
cleanup.

Screenshot: `26_final_verification.png`

## Why some commands differ from the assignment sheet

A few commands were made more explicit or adjusted so the lab would use the
intended Python runtime, controller endpoint, and OpenFlow version.

| Assignment-sheet command or approach | Command used in this lab | Reason |
|---|---|---|
| `python3 -m venv ryu-env` | `python3.10 -m venv ~/ryu-env` | The Ryu environment was created with Python 3.10 to use a compatible Python runtime rather than relying on whichever Python version `python3` points to on the VM. |
| `ryu-manager ryu.app.simple_switch_13` | `ryu-manager --ofp-tcp-listen-port 6653 ryu.app.simple_switch_13` | Makes the controller's OpenFlow listening port explicit so it matches the Mininet switch connection. |
| `sudo mn --topo single,3 --controller=remote --switch ovsk` | `sudo mn --topo single,3 --controller=remote,ip=127.0.0.1,port=6653 --switch ovsk,protocols=OpenFlow13` | Explicitly sets the controller address, port, and OpenFlow version. This avoids ambiguity about which controller endpoint and protocol the switch should use. |
| `h1 ping h2` | `h1 ping -c 4 h2` | Limits the test to four packets so it finishes promptly and produces a concise screenshot. |
| `del-flows s1 "priority=100,in_port=1"` | `del-flows s1` | The attempted selective deletion filter was rejected by `ovs-ofctl` (`unknown keyword priority`). The broad deletion command was used instead; it removes all flow entries, not only the DROP rule. |

**Important note about flow deletion:** `sudo ovs-ofctl -O OpenFlow13 del-flows s1`
does not preserve Ryu's learned forwarding entries. It removes all flows on
the switch. This is acceptable for the cleanup sequence shown here, but it
should not be described as deleting only the filtering rule.

**Compatibility note:** Ryu and its Eventlet dependency can be sensitive to
Python and package versions. If Ryu fails to start with an Eventlet-related
error, resolve the dependency/runtime compatibility issue before continuing;
a successful `pip install` alone does not prove that the controller starts.

## Result

The SDN lab environment was configured using Mininet, Open vSwitch, and the
Ryu controller. A three-host topology was created, host connectivity was
tested, OpenFlow 1.3 flow entries were inspected, and a DROP rule was applied
to demonstrate traffic filtering. The filtering test showed that traffic
from H1 was blocked while the rule was active. The flow entries were then
removed and Mininet was cleaned up.

## Screenshot index

Place the screenshots in this `day-4/` folder using the filenames below.

1. `01_ubuntu_update.png`
2. `02_install_mininet.png`
3. `03_install_ovs.png`
4. `04_ovs_status.png`
5. `05_mininet_start.png`
6. `06_mininet_nodes.png`
7. `07_mininet_pingall.png`
8. `08_python_venv_install.png`
9. `09_create_ryu_env.png`
10. `10_activate_ryu_env.png`
11. `11_upgrade_pip.png`
12. `12_install_ryu.png`
13. `13_ryu_version.png`
14. `14_ryu_controller.png`
15. `15_sdn_topology.png`
16. `16_sdn_nodes.png`
17. `17_network_connections.png`
18. `18_pingall_success.png`
19. `19_h1_h2_ping.png`
20. `20_flow_rules.png`
21. `21_add_filter_rule.png`
22. `22_filtering_test.png`
23. `23_remove_flow_rules.png`
24. `24_exit_mininet.png`
25. `25_mininet_cleanup.png`
26. `26_final_verification.png`
