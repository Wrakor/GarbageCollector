package elements.containers;

import elements.MapElement;
import elements.trucks.Truck;
import exceptions.ContainerFullException;

public abstract class Container extends MapElement {

	public static int defaultCapacity = 20;

	Integer capacity, usedCapacity, maxUsedCapacity = 0;

	public Container(Integer capacity) {
		this.capacity = capacity;
		if (this.capacity != null)
			this.usedCapacity = 0;
	}

	public abstract int getType();

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void emptyContainer() {
		this.usedCapacity = 0;
	}

	public void addToContainer(int ammount) throws ContainerFullException {
		if ((this.usedCapacity + ammount) > this.capacity)
			throw new ContainerFullException();
		else {
            this.usedCapacity += ammount;
            if (this.usedCapacity > this.maxUsedCapacity)
                this.maxUsedCapacity = this.usedCapacity;

        }
	}

    public Integer getMaxUsedCapacity() {
        return maxUsedCapacity;
    }

    public boolean isEmpty() {
		return usedCapacity == 0;
	}

	public int getUsedCapacity() {
		return usedCapacity;
	}

	public boolean truckCompatible(Truck truck) {
		return truck.getType() == this.getType();
	}
}
