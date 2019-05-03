package com.interaction.example.odata.northwind;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import org.junit.Ignore;
import org.junit.Test;

public class QueryOptionAtomITCase extends AbstractNorthwindRuntimeTest {

  public QueryOptionAtomITCase(RuntimeFacadeType type) {
    super(type);
  }

  @Test
  public void SystemQueryOptionOrderByTest() {
    String inp = "SystemQueryOptionOrderByTest";
    String uri = "Products?$top=20&$orderby=ProductID";
   testAtomResult(endpointUri, uri, inp);
  }

//  @Test
  public void SystemQueryOptionOrderByDescTest() {
    String inp = "SystemQueryOptionOrderByDescTest";
    String uri = "Products?$top=10&$orderby=ProductID desc";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionTopTest() {
    String inp = "SystemQueryOptionTopTest";
    String uri = "Products?$top=5";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionOrderByTopTest() {
    String inp = "SystemQueryOptionOrderByTopTest";
    String uri = "Products?$top=5&$orderby=ProductName desc";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionSkipTest() {
    String inp = "SystemQueryOptionSkipTest";
    String uri = "Categories(1)/Products?$skip=2";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionOrderBySkipTest() {
    String inp = "SystemQueryOptionOrderBySkipTest";
    String uri = "Products?$skip=2&$top=2&$orderby=ProductName";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionSkipTokenTest() {
    String inp = "SystemQueryOptionSkipTokenTest";
    String uri = "Customers?$top=5&$skiptoken='ANATR'";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionSkipTokenComplexKeyTest() {
    String inp = "SystemQueryOptionSkipTokenComplexKeyTest";
    String uri = "Order_Details?$top=5&$skiptoken=OrderID=10248,ProductID=11";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionSkipTokenWithOrderByTest() {
    String inp = "SystemQueryOptionSkipTokenWithOrderByTest";
    String uri = "Products?$orderby=SupplierID desc, ProductName&$skiptoken=20,'Gula Malacca',44";
   testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterEqualTest() {
    String inp = "SystemQueryOptionFilterEqualTest";
    String uri = "Suppliers?$filter=Country eq 'Brazil'";
   testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SystemQueryOptionFilterEqualPlusTest() {
    String inp = "SystemQueryOptionFilterEqualTest";
    String uri = "Suppliers?$filter=Phone eq '%2B11 555 4640'";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterGreaterThanTest() {
    String inp = "SystemQueryOptionFilterGreaterThanTest";
    String uri = "Products?$top=20&$filter=UnitPrice gt 20";
    testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterGreaterThanOrEqualTest() {
    String inp = "SystemQueryOptionFilterGreaterThanOrEqualTest";
    String uri = "Products?$top=20&$filter=UnitPrice ge 10";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterLessThanTest() {
    String inp = "SystemQueryOptionFilterLessThanTest";
    String uri = "Products?$top=20&$filter=UnitPrice lt 20";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterLessThanOrEqualTest() {
    String inp = "SystemQueryOptionFilterLessThanOrEqualTest";
    String uri = "Products?$top=20&$filter=UnitPrice le 100";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterLogicalAndTest() {
    String inp = "SystemQueryOptionFilterLogicalAndTest";
    String uri = "Products?$top=20&$filter=UnitPrice le 200 and UnitPrice gt 3.5";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterLogicalOrTest() {
    String inp = "SystemQueryOptionFilterLogicalOrTest";
    String uri = "Products?$filter=UnitPrice le 3.5 or UnitPrice gt 200";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionFilterGroupingLogicalAndTest() {
    String inp = "SystemQueryOptionFilterGroupingLogicalAndTest";
    String uri = "Products?$top=10&$filter=(UnitPrice gt 5) and (UnitPrice lt 20)";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SystemQueryOptionOrderByTop21Test() {
    String inp = "SystemQueryOptionOrderByTop21";
    String uri = "Products?$top=21&$orderby=ProductID";
   testAtomResult(endpointUri, uri, inp);
  }

  @Test
  @Ignore
  public void SystemQueryOptionExpand1Test() {
    String inp = "SystemQueryOptionExpand1Test";
    String uri = "Categories?$expand=Products";
    testAtomResult(endpointUri, uri, inp);
  }

  @Test
  @Ignore
  public void SystemQueryOptionExpand2Test() {
	  String inp = "SystemQueryOptionExpand2Test";
	  String uri = "Categories?$expand=Products/Supplier";
	  testAtomResult(endpointUri, uri, inp);
  }

  @Test
  @Ignore
  public void SystemQueryOptionExpand3Test() {
    String inp = "SystemQueryOptionExpand3Test";
    String uri = "Products?$expand=Category,Supplier";
   testAtomResult(endpointUri, uri, inp);
  }

  @Test
  @Ignore
  public void SystemQueryOptionExpand4Test() {
    String inp = "SystemQueryOptionExpand4Test";
    String uri = "Orders?$top=10&$orderby=OrderID&$expand=OrderDetails/Product";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SelectOnSingleEntityTest() {
    String inp = "SelectOnSingleEntityTest";
    String uri = "Products(1)?$select=ProductName";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void MultiSelectOnSingleEntityTest() {
    String inp = "MultiSelectOnSingleEntityTest";
    String uri = "Products(1)?$select=ProductName,UnitPrice";
   testJSONResult(endpointUri, uri, inp);
  }

  @Test
  @Ignore
  public void ExpandOnSingleEntityTest() {
    String inp = "ExpandOnSingleEntityTest";
    String uri = "Products(1)?$expand=Category";
   testAtomResult(endpointUri, uri, inp);
  }

  //@Test
  public void SelectExpandOnSingleEntityTest() {
    String inp = "SelectExpandOnSingleEntityTest";
    String uri = "Products(1)?$select=ProductName,Category&$expand=Category";
   testAtomResult(endpointUri, uri, inp);
  }

  @Test
  public void SelectTopZeroEntitiesTest() {
    String inp = "SelectTopZeroEntitiesTest";
    String uri = "Products?$top=0";
    testAtomResult(endpointUri, uri, inp);
  }
}
