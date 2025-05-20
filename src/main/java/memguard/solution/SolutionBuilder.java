package memguard.solution;

import java.util.ArrayList;
import java.util.List;

import memguard.solution.items.ComputationItem;
import memguard.solution.items.EmptyItem;
import memguard.solution.items.SharedResourceItem;
import memguard.solution.items.SolutionItem;
import memguard.solution.items.StallItem;

public class SolutionBuilder {

	private List<List<SolutionItem>> solutionItems;

	public SolutionBuilder(int processorNumbers) {
		solutionItems = new ArrayList<List<SolutionItem>>();

		for (int i = 0; i < processorNumbers; i++) {
			solutionItems.add(new ArrayList<SolutionItem>());
		}
	}

	public void addItemForAll(SolutionItem item, int processorIndex) {
		SolutionItem emptyItem = new EmptyItem(item.getLength());
		for (int i = 0; i < solutionItems.size(); i++) {
			if (i == processorIndex) {
				solutionItems.get(i).add(item);
			} else {
				solutionItems.get(i).add(emptyItem);
			}
		}
	}

	public void addItem(SolutionItem item, int processorIndex) {
		solutionItems.get(processorIndex).add(item);
	}

	public void addComputation(int length, int processorIndex, boolean forAll) {
		SolutionItem item = new ComputationItem(processorIndex, length);
		if (forAll) {
			addItemForAll(item, processorIndex);
		} else {
			addItem(item, processorIndex);
		}
	}

	public void addSharedResource(int length, int processorIndex, boolean forAll) {
		SolutionItem item = new SharedResourceItem(processorIndex, length);
		if (forAll) {
			addItemForAll(item, processorIndex);
		} else {
			addItem(item, processorIndex);
		}
	}

	public void addStall(int length, int processorIndex, boolean forAll) {
		SolutionItem item = new StallItem(length);
		if (forAll) {
			addItemForAll(item, processorIndex);
		} else {
			addItem(item, processorIndex);
		}
	}

	public void addVoid(int length, int processorIndex) {
		SolutionItem item = new EmptyItem(length);
		addItem(item, processorIndex);
	}

	public void addVoid(int length) {
		SolutionItem item = new EmptyItem(length);
		addItemForAll(item, 0);
	}

	public void addBeginPeriod(int processorIndex) {
		SolutionItem item = new EmptyItem(0, true, false);
		addItem(item, processorIndex);
	}

	public void addBeginPeriod() {
		SolutionItem item = new EmptyItem(0, true, false);
		for (int i = 0; i < solutionItems.size(); i++) {
			solutionItems.get(i).add(item);
		}
	}

	public void addEndPeriod(int processorIndex) {
		SolutionItem item = new EmptyItem(0, false, true);
		addItem(item, processorIndex);
	}

	public void addEndPeriod() {
		SolutionItem item = new EmptyItem(0, false, true);
		for (int i = 0; i < solutionItems.size(); i++) {
			solutionItems.get(i).add(item);
		}
	}

	public void removeLast(int processorIndex) {
		solutionItems.get(processorIndex).removeLast();
	}

	public void removeLast() {
		for (int i = 0; i < solutionItems.size(); i++) {
			if (!solutionItems.get(i).isEmpty()) {
				solutionItems.get(i).removeLast();
			}
		}
	}

	public Solution build(SolutionStatus status) {
		SolutionItem[][] items = new SolutionItem[solutionItems.size()][];
		for (int i = 0; i < items.length; i++) {
			items[i] = solutionItems.get(i).toArray(new SolutionItem[0]);
		}
		
		return new Solution(status, items);
	}
}
