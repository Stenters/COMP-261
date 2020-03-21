public class RefTest {

    static class Ref {
        String val;
        Ref otherRef;
    }

    public static void main(String[] args) {
        Ref r1 = new Ref();
//        setRef(r1);
        setRef2(r1);
        System.out.println(r1.val);
        System.out.println(r1.otherRef);
        r1.val.equalsIgnoreCase("");
        if (r1.val.equals("") || r1.otherRef == null ) {
            System.out.println("Ref failed!");
        }
        else {
            System.out.println("Succeeded!");
        }

        System.out.printf("%s,%s\n", r1.val,r1.otherRef);
    }

    private static void setRef2(Ref r1) {
        r1 = new Ref();
        r1.val= "Changed!";
        r1.otherRef = new Ref();
    }

    private static void setRef(Ref r1) {
        r1.val = "Rewritten";
        r1.otherRef = new Ref();
    }

}
