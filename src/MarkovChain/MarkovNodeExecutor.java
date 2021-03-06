package MarkovChain;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MarkovNodeExecutor {

    private final HashMap<Class<? extends ExecutableNode>, ExecutableNode> classTypeMap;
    private ExecutableNode current;

    public MarkovNodeExecutor(ExecutableNode startingNode, ExecutableNode... nodes){
        current = startingNode;
        classTypeMap = new HashMap<>(16);
        classTypeMap.put(startingNode.getClass(), startingNode);
        for(ExecutableNode n: nodes){
            classTypeMap.put(n.getClass(), n);
        }
    }

    /*
    executes the current node then returns the sleeptime until the next onLoop call.
    All ExecutableNodes implement a execute method that returns an int, That int is the sleeptime for until the next onLoop call.
     */
    public int executeThenTraverse() throws InterruptedException {
        int onLoopSleepTime = 500;
        if(current.canExecute()){
            onLoopSleepTime = current.executeNode();
        }
        if(current.isJumping()) {
            ExecutableNode temp = classTypeMap.get(current.setJumpTarget());
            if(temp != null){
                current = temp;
            } else {
                throw new NullPointerException("jump target of " + current.getClass().getSimpleName() + " is null");
            }
        }
        else{
            normalTraverse();
        }

        return onLoopSleepTime;
    }

    private void normalTraverse(){
        if(current != null){
            List<Edge> edges = current.getAdjacentNodes();
            if(edges == null || edges.size() == 0){
                throw new NullPointerException("no outgoing edges off " + current.getClass().getSimpleName());
            }
            // Algorithm for random percentage branching
            // https://stackoverflow.com/questions/45836397/coding-pattern-for-random-percentage-branching?noredirect=1&lq=1
            int combinedWeight = edges.stream().mapToInt(Edge::getCurrentExecutionWeight).sum();
            int sum = 0;
            int roll = ThreadLocalRandom.current().nextInt(1, combinedWeight+1);
            ExecutableNode selected = null;
            for(Edge e: edges){
                sum += e.getCurrentExecutionWeight();
                if(sum >= roll){
                    selected = classTypeMap.get(e.getV());
                    break;
                }
            }
            if(selected == null){
                selected = classTypeMap.get(edges.get(edges.size()-1).getV());
            }
            current = selected;
        }
    }
}