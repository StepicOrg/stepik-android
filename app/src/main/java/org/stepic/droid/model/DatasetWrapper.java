package org.stepic.droid.model;

public class DatasetWrapper {
    private Dataset dataset;

    public DatasetWrapper() {
        dataset = null;
    }

    public DatasetWrapper(Dataset dataset) {
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }
}
