package org.zof.deswing.model;

import ysoserial.Strings;
import ysoserial.payloads.ObjectPayload;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class PayloadTableModel extends DefaultTableModel {
    private Vector<Vector<String>> tableData;
    public PayloadTableModel() {
        tableData = initTableModel();
        Vector<String> columns = new Vector<>();
        columns.add("Payload");
        columns.add("Authors");
        columns.add("Dependencies");
        setDataVector(tableData,columns);
    }
    public String getPayloadType(int i){
        return tableData.get(i).get(0);
    }
    private Vector<Vector<String>> initTableModel() {
        Vector<Vector<String>> data = new Vector<>();
        final List<Class<? extends ObjectPayload>> payloadClasses =
                new ArrayList<>(ObjectPayload.Utils.getPayloadClasses());
        payloadClasses.sort(new Strings.ToStringComparator()); // alphabetize

        for (Class<? extends ObjectPayload> payloadClass : payloadClasses) {
            Vector<String> row = new Vector<>();
            row.add(payloadClass.getSimpleName());
            row.add(Strings.join(Arrays.asList(Authors.Utils.getAuthors(payloadClass)), ", ", "@", ""));
            row.add(Strings.join(Arrays.asList(Dependencies.Utils.getDependenciesSimple(payloadClass)),", ", "", ""));
            data.add(row);
        }
        return data;
    }
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
