public class tst {
public int main(String[] args)
{
    int x = 100;

    while(args.length != 0){
        if(x < 200)
            x = 100;
        else
            x = 200;
    }

    return x;
}
}
