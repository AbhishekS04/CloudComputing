
package lab;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmScheduler;
import org.cloudsimplus.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.utilizationmodels.UtilizationModel;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.provisioners.ResourceProvisionerSimple;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;

public class SchedulingExperiment {

    private static final int HOST_PES = 4;
    private static final int VM_COUNT = 2;
    private static final int CLOUDLET_COUNT = 4;

    private static final long MIPS = 1000;
    private static final long CLOUDLET_LENGTH = 20000;

    public static void main(String[] args) {

        runScenario(
                "A: VM SpaceShared + Cloudlet SpaceShared",
                new VmSchedulerSpaceShared(),
                CloudletSchedulerSpaceShared::new
        );

        runScenario(
                "B: VM SpaceShared + Cloudlet TimeShared",
                new VmSchedulerSpaceShared(),
                CloudletSchedulerTimeShared::new
        );

        runScenario(
                "C: VM TimeShared + Cloudlet SpaceShared",
                new VmSchedulerTimeShared(),
                CloudletSchedulerSpaceShared::new
        );

        runScenario(
                "D: VM TimeShared + Cloudlet TimeShared",
                new VmSchedulerTimeShared(),
                CloudletSchedulerTimeShared::new
        );
    }

    private static void runScenario(
            String title,
            VmScheduler vmScheduler,
            Supplier<CloudletScheduler> cloudletSchedulerFactory) {

        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("==================================================");

        CloudSimPlus simulation = new CloudSimPlus();

        createDatacenter(simulation, vmScheduler);

        DatacenterBroker broker =
                new DatacenterBrokerSimple(simulation);

        List<Vm> vmList = createVms(cloudletSchedulerFactory);
        List<Cloudlet> cloudletList = createCloudlets();

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        simulation.start();

        List<Cloudlet> finished =
                broker.getCloudletFinishedList();

        System.out.printf(
                "%-10s %-10s %-12s %-12s %-12s%n",
                "Cloudlet", "VM", "Status", "Start", "Finish"
        );

        for (Cloudlet cloudlet : finished) {

            System.out.printf(
                    "%-10d %-10d %-12s %-12.2f %-12.2f%n",
                    cloudlet.getId(),
                    cloudlet.getVm().getId(),
                    cloudlet.getStatus(),
                    cloudlet.getStartTime(),
                    cloudlet.getFinishTime()
            );
        }

        System.out.println(
                "Completed Cloudlets: "
                        + finished.size()
                        + " / "
                        + CLOUDLET_COUNT
        );
    }

    private static DatacenterSimple createDatacenter(
            CloudSimPlus simulation,
            VmScheduler vmScheduler) {

        List<Pe> peList = new ArrayList<>();

        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(MIPS));
        }

        Host host = new HostSimple(
                8192,
                100000,
                1000000,
                peList
        );

        host.setRamProvisioner(
                new ResourceProvisionerSimple()
        );

        host.setBwProvisioner(
                new ResourceProvisionerSimple()
        );

        host.setVmScheduler(vmScheduler);

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        return new DatacenterSimple(
                simulation,
                hostList,
                new VmAllocationPolicySimple()
        );
    }

    private static List<Vm> createVms(
            Supplier<CloudletScheduler> schedulerFactory) {

        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VM_COUNT; i++) {

            Vm vm = new VmSimple(i, MIPS, 1);

            vm.setRam(2048);
            vm.setBw(10000);
            vm.setSize(10000);

            vm.setCloudletScheduler(schedulerFactory.get());

            vmList.add(vm);
        }

        return vmList;
    }

    private static List<Cloudlet> createCloudlets() {

        List<Cloudlet> cloudletList = new ArrayList<>();

        UtilizationModel cpuUtilization =
                new UtilizationModelFull();

        UtilizationModel ramUtilization =
                new UtilizationModelDynamic(0);

        UtilizationModel bwUtilization =
                new UtilizationModelDynamic(0);

        for (int i = 0; i < CLOUDLET_COUNT; i++) {

            Cloudlet cloudlet = new CloudletSimple(
                    CLOUDLET_LENGTH,
                    1,
                    cpuUtilization
            );

            cloudlet.setUtilizationModelRam(ramUtilization);
            cloudlet.setUtilizationModelBw(bwUtilization);

            cloudlet.setSizes(300);

            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
}
