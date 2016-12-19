package web.com.data_mining.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Instance;
import weka.core.Instances;

public class BayesianNet {
	private List<List<Integer>> inGf;
	private List<List<Integer>> outGf;
	private List<Map<String, List<Double>>> condProbTable;
	private List<Map<String, Integer>> attributeValues;
	private Instances data;
	private int classIndex;
	private static int INF = (1 << 29);

	public BayesianNet(Instances data) {
		this.data = data;
		classIndex = data.numAttributes() - 1;// ar trebuie sa fie
												// data.classIndex();
	}

	public Double getProb(Integer attributeIndex, String attributeState, Map<Integer, String> parentsState) {
		String internalRepresentation = "";
		List<Integer> parents = inGf.get(attributeIndex);
		for (int i = 0; i < parents.size(); ++i) {
			internalRepresentation += parentsState.get(parents.get(i));
		}
		int attributeValueOrder = attributeValues.get(attributeIndex).get(attributeState);
		Map<String, List<Double>> myMap = condProbTable.get(attributeIndex);
		System.out.println(attributeIndex + " " + " " + attributeState + " " + parents.size() + " " + internalRepresentation + " " + myMap.containsKey(internalRepresentation));
		return condProbTable.get(attributeIndex).get(internalRepresentation).get(attributeValueOrder);
	}

	private void initParam() {
		inGf = new ArrayList<List<Integer>>();
		outGf = new ArrayList<List<Integer>>();
		condProbTable = new ArrayList<Map<String, List<Double>>>();
		attributeValues = new ArrayList<Map<String, Integer>>();
		for (int i = 0; i < data.numAttributes(); ++i) {
			inGf.add(new ArrayList<>());
			outGf.add(new ArrayList<>());
			condProbTable.add(new HashMap<String, List<Double>>());
			Map<String, Integer> currMap = new HashMap<>();
			System.out.println("muie2: ceva" + i + " " + data.attribute(i).name());
			for (int j = 0; j < data.attribute(i).numValues(); ++j) {
				System.out.println("muie" + data.attribute(i).value(j) +  j);
				currMap.put(data.attribute(i).value(j), j);
			}
			attributeValues.add(currMap);
		}
	}

	// public long getAlpha(int i, int j, int k){
	//
	// }
	//
	public void learn() {
		initParam();
		//learnStructureNaive();
		learnStrucureK2();
		// test();
		System.out.println("----------->>>>>>>>>>>>>>---------");
		learnCPT();
		System.out.println("ceva");
	}

	void test() {
		getCost(3, getParents(3));
	}

	// private int getNij(List<Integer> dads, List<String> state){
	// int cnt = 0;
	// System.out.println("parinti:" );
	// for(int i=0; i<dads.size(); ++i){
	// System.out.print(data.attribute(dads.get(i)).name());
	// }
	// System.out.println(dads.size() + " " + state.size());
	// for(int i=0; i<dads.size(); ++i){
	// System.out.print(state.get(i) + " ");
	// }
	//
	// for(int i=0; i<data.numInstances(); ++i){
	// Instance instance = data.get(i);
	// boolean ok = true;
	// for(int j=0; j<dads.size(); ++j){
	// int currAttibuteIndex = dads.get(j);
	// if ( !instance.stringValue(currAttibuteIndex).equals(state.get(j)) ) {
	// ok = false;
	// break;
	// }
	// }
	// if (ok){
	// ++cnt;
	// }
	// }
	// System.out.println("\ncnt= " + cnt);
	// return cnt;
	// }

	private int getNij(int node, List<Integer> dads, List<String> state) {
		List<Integer> cnts = getAlphaIJKList(node, dads, state);
		int sum = 0;
		for (int i = 0; i < cnts.size(); ++i) {
			sum += cnts.get(i);
		}
		return sum;
	}

	double getLogFactorial(int x) {
		double sum = 0.0;
		for (int i = 1; i <= x; ++i) {
			sum += Math.log(i);
		}
		return sum;
	}

	private double getCost(int node, List<Integer> dads) {
		double cost = 0.0;
		List<List<String>> dadsInstances = getDadsInstances(node, dads);
		if (dadsInstances.isEmpty()){
			return -1.0*INF;
		}
		System.out.println("get cost for node:" + node);
		System.out.print("parents: ");
		for(int i=0; i<dads.size(); ++i){
			System.out.print(dads.get(i) + " ");
		}
		System.out.println();
		
		for (int i = 0; i < dadsInstances.size(); ++i) {
			int ri = data.attribute(node).numValues();
			int Nij = getNij(node, dads, dadsInstances.get(i));
			List<Integer> alphaijkList = getAlphaIJKList(node, dads, dadsInstances.get(i));
			double right = 0.0;
			for (int j = 0; j < alphaijkList.size(); ++j) {
				right += getLogFactorial(alphaijkList.get(j));
			}
			String s = "";
			for(int j=0; j<dadsInstances.get(i).size(); ++j){
				s += dadsInstances.get(i).get(j) +"-";
			}
			for(int j=0; j<alphaijkList.size(); ++j){
				s += alphaijkList.get(j) + "--";
			}
			System.out.println(s);
			double left = 0.0;
			int a = ri-1;
			int b = Nij+ri-1;
			int minn = a;
			int maxx = b;
			if (minn > maxx){
				int temp = minn;
				minn = maxx;
				maxx = temp;
			}
			for(int ii=minn+1; ii<=maxx; ++ii){
				left += Math.log(ii);
			}
			//double diff = leftUp - leftDown;
			cost += left + right;
		}
		return cost;
	}

	List<Integer> getAlphaIJKList(int attributeIndex, List<Integer> dads, List<String> state) {
		Integer cnts[] = new Integer[data.attribute(attributeIndex).numValues()];
		for (int i = 0; i < data.attribute(attributeIndex).numValues(); ++i) {
			cnts[i] = 1;
		}
		// System.out.println("parinti:" );
		// for(int i=0; i<dads.size(); ++i){
		// System.out.print(data.attribute(dads.get(i)).name());
		// }
		// System.out.println(dads.size() + " " + state.size());
		// for(int i=0; i<dads.size(); ++i){
		// System.out.print(state.get(i) + " ");
		// }
		//
//		String s = "";
//		for(int i=0; i<dads.size(); ++i){
//			s += "(" + state.get(i) + "," + data.attribute(dads.get(i)).numValues() + ")";
//		}
//		System.out.println(s);
		for (int i = 0; i < data.numInstances(); ++i) {
			Instance instance = data.get(i);
			boolean ok = true;
			for (int j = 0; j < dads.size(); ++j) {
				int currAttibuteIndex = dads.get(j);
				if (!instance.stringValue(currAttibuteIndex).equals(state.get(j))) {
					ok = false;
					break;
				}
			}
			if (ok) {
				int attributeValueOrder = attributeValues.get(attributeIndex)
						.get(instance.stringValue(attributeIndex));
				cnts[attributeValueOrder]++;
			}
		}
		// System.out.println("\ncnt= ");
		// for(int i=0; i<data.attribute(attributeIndex).numValues(); ++i){
		// System.out.println( data.attribute(attributeIndex).value(i) + " " +
		// cnts[i] + " ");
		// }
		return (List<Integer>) Arrays.asList(cnts);

	}

	public List<List<String>> getDadsInstances(int node, List<Integer> dads) {
		List<List<String>> dadsInstances = new ArrayList<>();
		String[] values = new String[dads.size()];
		back(node, dads, 0, values, dadsInstances);
		return dadsInstances;
	}

	int getBestDad(int node, List<Integer> currDads, Set<Integer> dads) {
		double cost = -1.0 * INF;
		int dad = -1;
		for (Integer currDad : dads) {
			if (!currDads.contains(currDad)) {
				currDads.add(currDad);
				double currCost = getCost(node, currDads);
				System.out.println("aciiiiiiiiii" + node + " " + currDad + " " + currCost + " " + cost);
				currDads.remove(currDads.size() - 1);
				if (currCost > cost) {
					cost = currCost;
					dad = currDad;
				}
			}
		}
		return dad;
	}
	
	public List<Integer> getNodes(){
		List<Integer> list = new ArrayList<>();
		for(int i=0; i<data.numAttributes(); ++i){
			list.add(i);
		}
		return list;
	}
	
	public Integer getIndexClass(){
		return classIndex;
	}
	
	private void learnStrucureK2() {
		Set<Integer> dads = new HashSet<>();
		int numberOfMaxNeighbors = 2;
		dads.add(classIndex);
		List<Integer> nodes = new ArrayList<>();
		for (int i = 0; i < data.numAttributes(); ++i) {
			if (i != classIndex) {
				nodes.add(i);
			}
		}
		Collections.shuffle(nodes);
		for (int i = 0; i < nodes.size(); ++i) {
			int currNode = nodes.get(i);
			List<Integer> currDads = new ArrayList<>();
			boolean done = false;
			double currCost = getCost(currNode, currDads);
			System.out.println("try to find dads for " + currNode + " having initial cost:" + currCost);
			System.out.print("nodes in the graph so far: ");
			for(int x : dads){
				System.out.print(x + ",");
			}
			System.out.println();
			
			while (!done && currDads.size() < numberOfMaxNeighbors) {
				int bestDad = getBestDad(currNode, currDads, dads);
				if (bestDad == -1) {
					//System.out.println("ceva nu e bine nu a  gasit ");
					done = true;
					break;
				}
				currDads.add(bestDad);
				double newCurrScore = getCost(currNode, currDads);
				System.out.println("found " + bestDad + "with score " + newCurrScore + " and old score: " + currCost);
				if (newCurrScore > currCost) {
					currCost = newCurrScore;
				} else {
					done = true;
					currDads.remove(currDads.size() - 1);
				}
			}
			if (currDads.isEmpty()) {
				System.out.println("ceva nu e in regula nu a fasit nici un tata nodeul e " + currNode);
				currDads.add(classIndex);
			}
			System.out.println("parents: " );
			for (int ii = 0; ii < currDads.size(); ++ii) {
				int tata = currDads.get(ii);
				System.out.print(tata + ", ");
				addArc(tata, currNode);
			}
			System.out.println();
			dads.add(currNode);
		}

	}

	public List<Integer> getParents(int attributeIndex) {
		return inGf.get(attributeIndex);
	}

	void back(int attributeIndex, List<Integer> parrentSet, int currIndex, String[] currParentState,
			List<List<String>> parentsInstances) {
		if (currIndex == parrentSet.size()) {
			List<String> currParentStateCopy = new ArrayList<>();
			for (int i = 0; i < currIndex; ++i) {
				currParentStateCopy.add(currParentState[i]);
			}
			if (currParentStateCopy.size() > 0 )parentsInstances.add(currParentStateCopy);
		} else {
			for (int i = 0; i < data.attribute(parrentSet.get(currIndex)).numValues(); ++i) {
				currParentState[currIndex] = data.attribute(parrentSet.get(currIndex)).value(i);
				back(attributeIndex, parrentSet, currIndex + 1, currParentState, parentsInstances);
			}
		}
	}
	
	private void fix(List<Integer> counts){
		for(int i=0; i<counts.size(); ++i){
			counts.set(i, counts.get(i)-1);
		}
	}

	private void learnCPT() {
		for (int i = 0; i < data.numAttributes(); ++i) {
			System.out.println(data.attribute(i).name() + " " + i);
			List<Integer> parentSet = getParents(i);
			List<List<String>> parentsInstances = getDadsInstances(i, parentSet);
			if (i == classIndex){
				List<Integer> counts = getAlphaIJKList(i, parentSet, new ArrayList<String>());
				fix(counts);
				double total = 0.0;
				for (int k = 0; k < counts.size(); ++k) {
					total += (counts.get(k));
				}
				// add to total; miu
				int miu = counts.size();
				total += miu;
				String internalRepresentation = "";
				List<Double> prob = new ArrayList<>();
				System.out.println(internalRepresentation + " : ");
				for (int k = 0; k < counts.size(); ++k) {
					double curr = 1.0 * counts.get(k) + 1;
					prob.add(curr / total);
					System.out.print(prob.get(k) + " ");
				}
				System.out.println();
				Map<String, List<Double>> myMap = condProbTable.get(i);
				myMap.put(internalRepresentation, prob);
			}
			for (int ii = 0; ii < parentsInstances.size(); ++ii) {
				List<String> currState = parentsInstances.get(ii);
				String internalRepresentation = "";
				for (int j = 0; j < currState.size(); ++j) {
					// System.out.print(parentsInstances.get(ii).get(j) + " ");
					internalRepresentation += currState.get(j);
				}
				List<Integer> counts = getAlphaIJKList(i, parentSet, currState);
				fix(counts);
				double total = 0.0;
				for (int k = 0; k < counts.size(); ++k) {
					total += (counts.get(k));
				}
				// add to total; miu
				int miu = counts.size();
				total += miu;

				List<Double> prob = new ArrayList<>();
				System.out.println(internalRepresentation + " : ");
				for (int k = 0; k < counts.size(); ++k) {
					double curr = 1.0 * counts.get(k) + 1;
					prob.add(curr / total);
					System.out.print(prob.get(k) + " ");
				}
				Map<String, List<Double>> myMap = condProbTable.get(i);
				myMap.put(internalRepresentation, prob);
				System.out.println();
				
			}
		}
	}

	private void addArc(int x, int y) {
		outGf.get(x).add(y);
		inGf.get(y).add(x);
	}

	private void learnStructureNaive() {
		// TODO Auto-generated method stub
		// naive for moment;

		for (int i = 0; i < classIndex; ++i) {
			if (i == classIndex) {
				continue;
			}
			addArc(classIndex, i);
		}
		addArc(2, 3);
	}

	public String structureToString() {
		String s = "";
		for (int i = 0; i < data.numAttributes(); ++i) {
			s += i + " " + data.attribute(i).name() + ":";
			for (int j = 0; j < inGf.get(i).size(); ++j) {
				s += "(" + data.attribute(inGf.get(i).get(j)).name() + ", " + inGf.get(i).get(j) + ")";
			}
			s += "\n";
		}
		return s;
	}

}
