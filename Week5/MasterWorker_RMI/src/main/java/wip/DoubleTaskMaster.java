package wip;

import wip.GenericTaskMaster;

import java.rmi.RemoteException;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class DoubleTaskMaster extends GenericTaskMaster<Double> {
    public DoubleTaskMaster(int nTasks, Function<Integer, Double> taskCode, BinaryOperator<Double> resultAccumulator) throws RemoteException {
        super(nTasks, taskCode, resultAccumulator);
    }
}
