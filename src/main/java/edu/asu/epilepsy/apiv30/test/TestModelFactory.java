package edu.asu.epilepsy.apiv30.test;

import edu.asu.epilepsy.apiv30.service.PromisService;

public class TestModelFactory {
  public static void main(String[] args) {
    PromisService obj = new PromisService();
    try {
      System.out.println(obj.getActivityInstance("1", ""));
      //System.out.println(obj.checkActivityInstance(patienPIN));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
