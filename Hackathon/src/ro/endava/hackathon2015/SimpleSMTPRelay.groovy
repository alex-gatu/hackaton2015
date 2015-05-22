package ro.endava.hackathon2015

class SimpleSMTPRelay {
    public static void relay(from, raw){
        def heap=new ro.endava.hackathon2015.Heap('rules');
        def activeRule=null;
        def context=[from:from, raw:raw]
        heap.list().each{key->
            def rule=heap.get(key);
            if(rule['protocol']!='smtp')
                return;
            try{
                context.precondition=Eval.x(context,rule['precondition']);
            }catch(e){}
            if(context.precondition){
                activeRule=rule;
                return;
            }
        }
        if(activeRule)
            context.computation=Eval.x(context,activeRule['computation']);
        def rendition=Eval.x(context,activeRule['rendition'])?:''
        def mails=new ro.endava.hackathon2015.Heap('mails');
        mails.put rendition
    }
}
