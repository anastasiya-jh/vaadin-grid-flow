/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.ColumnBase;
import com.vaadin.flow.component.grid.ColumnGroup;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;

public class GridColumnTest {

    Grid<String> grid;
    Column<String> firstColumn;
    Column<String> secondColumn;
    Column<String> thirdColumn;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        grid = new Grid<>();
        firstColumn = grid.addColumn(str -> str);
        secondColumn = grid.addColumn(str -> str);
        thirdColumn = grid.addColumn(str -> str);
    }

    @Test
    public void setKey_getByKey() {
        firstColumn.setKey("foo");
        secondColumn.setKey("bar");
        Assert.assertEquals(firstColumn, grid.getColumnByKey("foo"));
        Assert.assertEquals(secondColumn, grid.getColumnByKey("bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void changeKey_throws() {
        firstColumn.setKey("foo");
        firstColumn.setKey("bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateKey_throws() {
        firstColumn.setKey("foo");
        secondColumn.setKey("foo");
    }

    @Test
    public void merged_column_order() {
        Assert.assertEquals(
                Arrays.asList(firstColumn, secondColumn, thirdColumn),
                getTopLevelColumns());
        ColumnGroup merged = grid.mergeColumns(firstColumn, thirdColumn);
        Assert.assertEquals(Arrays.asList(merged, secondColumn),
                getTopLevelColumns());
        ColumnGroup secondMerge = grid.mergeColumns(merged, secondColumn);
        Assert.assertEquals(Arrays.asList(secondMerge), getTopLevelColumns());
        Assert.assertEquals(
                Arrays.asList(firstColumn, thirdColumn, secondColumn),
                grid.getColumns());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cant_merge_columns_not_in_grid() {
        Column<String> otherColumn = new Grid<String>().addColumn(str -> str);
        grid.mergeColumns(firstColumn, otherColumn);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cant_merge_already_merged_columns() {
        grid.mergeColumns(firstColumn, secondColumn);
        grid.mergeColumns(firstColumn, thirdColumn);
    }

    @Test
    public void removeColumnByKey() {
        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeColumnByNullKey_throws() {
        expectNullPointerException("columnKey should not be null");
        grid.removeColumnByKey(null);
    }

    @Test
    public void removeColumn() {
        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeNullColumn_throws() {
        expectNullPointerException("column should not be null");
        grid.removeColumn(null);
    }

    @Test
    public void removeInvalidColumnByKey_throws() {
        expectIllegalArgumentException(
                "The column with key 'wrong' is not part of this Grid");

        grid.removeColumnByKey("wrong");
    }

    @Test
    public void removeColumnByKeyTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not part of this Grid");

        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        grid.removeColumnByKey("first");
    }

    @Test
    public void removeInvalidColumn_throws() {
        expectIllegalArgumentException(
                "The column with key 'wrong' is not part of this Grid");

        Grid<String> grid2 = new Grid<>();
        Column<String> wrongColumn = grid2.addColumn(str -> str);
        wrongColumn.setKey("wrong");
        grid.removeColumn(wrongColumn);
    }

    @Test
    public void removeColumnTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not part of this Grid");

        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        grid.removeColumn(firstColumn);
    }

    @Test
    public void removeMergedColumn() {
        ColumnGroup merged = grid.mergeColumns(firstColumn, secondColumn);

        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);

        Assert.assertThat(merged.getChildColumns(),
                CoreMatchers.not(CoreMatchers.hasItem(firstColumn)));
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeMergedColumns_columnGroupIsRemoved() {
        ColumnGroup merged = grid.mergeColumns(firstColumn, secondColumn);
        Assert.assertEquals(grid.getElement(), merged.getElement().getParent());

        grid.removeColumn(firstColumn);
        grid.removeColumn(secondColumn);

        Assert.assertEquals(0, merged.getChildColumns().size());
        Assert.assertNull(merged.getElement().getParent());
    }

    @Test
    public void removeAllMergedColumns_columnGroupsAreRemoved() {
        ColumnGroup merged = grid.mergeColumns(firstColumn, thirdColumn);
        ColumnGroup secondMerge = grid.mergeColumns(merged, secondColumn);
        Assert.assertEquals(secondMerge.getElement(),
                merged.getElement().getParent());
        Assert.assertEquals(grid.getElement(),
                secondMerge.getElement().getParent());

        grid.removeColumn(firstColumn);
        grid.removeColumn(secondColumn);
        grid.removeColumn(thirdColumn);

        Assert.assertEquals(0, merged.getChildColumns().size());
        Assert.assertNull(merged.getElement().getParent());
        Assert.assertEquals(0, secondMerge.getChildColumns().size());
        Assert.assertNull(secondMerge.getElement().getParent());
    }

    private List<ColumnBase<?>> getTopLevelColumns() {
        return grid.getElement().getChildren()
                .map(element -> element.getComponent())
                .filter(component -> component.isPresent()
                        && component.get() instanceof ColumnBase<?>)
                .map(component -> (ColumnBase<?>) component.get())
                .collect(Collectors.toList());
    }

    private void expectNullPointerException(String message) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(message);
    }

    private void expectIllegalArgumentException(String message) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(message);
    }
}