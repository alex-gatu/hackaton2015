<?def heap=new ro.endava.hackathon2015.Heap('rules');
def activeRule=null;
def context=[:];
context['_ARGS']=_ARGS;
context['_HEADERS']=_HEADERS;
context['exchange']=exchange;
context['httpConnection']=httpConnection;
heap.list().each{index->
	def rule=heap.get(index);
	if(rule['protocol']!='http')
		return;
	try{
		context.precondition=Eval.x(context,rule['precondition']);
	}catch(e){}
	if(context.precondition){
		activeRule=rule;
		return;
	}
}
if(!activeRule){?>
	<!DOCTYPE html>
	<html>
		<head>
			<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
			<meta charset='UTF-8'/>
			<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootswatch/3.0.3/yeti/bootstrap.min.css">
			<script type="text/javascript" src='//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js'></script>
			<title>Hackathon 2015</title>
		</head>
		<body>
			HTTP 404 - ești bou
		</body>
	</html>
<?}else{
	try{
		context.computation=Eval.x(context,activeRule['computation']);
	}catch(e){?>
		<tt><?&e?></tt>
	<?}?><?=Eval.x(context,activeRule['rendition'])?:''?><?}?>