// SELECT
//
// {select, [Filter, count]}
// result = type[count]
package team094;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import battlecode.common.*;

class SelectAction extends Action {
    final SelectFilter filter;
    final int count;
    final ArrayList<Object> data;

    // Results
    ArrayList<Object> results;


    SelectAction(Role r, SelectFilter f, int c, ArrayList<Object> d) {
        super(r);
        filter = f;
        count = c;
        data = d;
    }


    public ArrayList<Object> getResults() {
        return results;
    }
    public boolean canAct() {
        return (completed == false &&
                data != null &&
                count <= data.size() &&
                Clock.getBytecodesLeft() > data.size() * getEstIterCost());
    }
    public void act() throws GameActionException {
        class EvalPair {
            public final double value;
            public final Object obj;

            EvalPair(double v, Object o) { value = v; obj = o; }
        }
        ArrayList<EvalPair> evaluations = new ArrayList<EvalPair>(data.size());
        for (Object o: data) {
            evaluations.add(new EvalPair(filter.eval(o), o));
        }
        Collections.sort(evaluations, new Comparator<EvalPair>() {
            @Override
            public int compare(EvalPair e1, EvalPair e2) {
                return Double.compare(e1.value, e2.value);
            }
        });
        ArrayList<Object> tmp = new ArrayList<Object>(count);
        for (int i = 0; i < count; ++i)
            tmp.add(evaluations.get(i).obj);
        results = tmp;
        setComplete();
    }

    public SelectAction copy() {
        ArrayList<Object> d = new ArrayList<Object>();
        d.addAll(data);
        return new SelectAction(agent, filter, count, d);
    }
    public SelectAction copy(ArrayList<Object> d) {
        return new SelectAction(agent, filter, count, d);
    }
    public String toString() {
        return "{select, [" + filter + ", " + count + "]}";
    }


    protected void setComplete() {
        System.out.println("Completed action: " + this);
        completed = true;
    }
    private int getEstIterCost() {
        return 20;
    }
}
