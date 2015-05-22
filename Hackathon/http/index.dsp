<?def heap=new ro.endava.hackathon2015.Heap('rules');?>
<!DOCTYPE html>
<html>
	<head>
		<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
		<meta charset='UTF-8'/>
		<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
		<script type="text/javascript" src='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js'></script>
		<title>Hackathon 2015 <?if(_ARGS.rule){def rule=heap.get(_ARGS.rule)?:[:];?><?&rule['label']?><?}?></title>
		<style>
			input, textarea{width:100%}
			textarea{height:30em; font-family:monospace}
		</style>
	</head>
	<body class='container'>
		<datalist id="protocols">
			<option value="http">Generic HTTP Server</option>
			<option value="smtp">SMTP Server</option>
			<option value="amqp">AMQP Server</option>
			<option value="imap">IMAP Server</option>
			<option value="jms">JMS Client</option>
			<option value="http-soap">Dedicated SOAP</option>
			<option value="telnet">Telnet Echo</option>
		</datalist> 
		<nav class='navbar navbar-default'>
			<div class='navbar-header'>
				<a class='navbar-brand' href='?'><i class='glyphicon glyphicon-home'></i></a>
			</div>
			<ul class='nav navbar-nav'>
				<li><a href='?rule=<?=java.util.UUID.randomUUID()?>'><i class='glyphicon glyphicon-plus'></i> New rule</a></li>
			</ul>
		</nav>
		<?if(_ARGS.rule){?>
			<?def rule=heap.get(_ARGS.rule)?:[:];?>
			<?if(_ARGS._action=='save'){
				'protocol,label,description,precondition,computation,rendition,hot'.split(',').each{field->rule[field]=_ARGS[field];}
				heap.put(_ARGS.rule,rule);
			}?>
			<form method='post' action='?rule=<?=_ARGS.rule?>'>
				<input type='hidden' name='_action' value='save'/>
				<input type='hidden' name='rule' value='<?&_ARGS.rule?>'/>
				<div class='row'>
					<div class='col-lg-2'>
						<div class="input-group">
							<span class='input-group-addon'>Protocol</span>
							<input class="form-control" name='protocol' list='protocols' value='<?&rule['protocol']?:''?>'/>
						</div>
					</div>
					<div class='col-lg-4'>
						<div class="input-group">
							<span class='input-group-addon'>Label</span>
						<input class="form-control" name='label' value='<?&rule['label']?:''?>'/>
						</div>
					</div>
					<div class='col-lg-4'>
						<div class="input-group">
							<span class='input-group-addon'>Description</span>
						<input class="form-control" name='description' value='<?&rule['description']?:''?>'/>
						</div>
					</div>
					<div class='col-lg-1'>
						<i class='text-danger pull-right glyphicon glyphicon-fire'></i>
						<input name='hot' type='checkbox' <?=rule['hot']?'checked':''?>/>
					</div>
					<div class='col-lg-1'>
						<input class='btn btn-success' type='submit' accesskey='s' value='Save'/>
					</div>
				</div>

				<hr/>
				<?def error=null;?>
				<div class='row'>
					<?try{Eval.me('({'+rule['precondition']+'})');}catch(e){error=e;}?>
					<div class='col-lg-6'>
						<div class='panel panel-<?=error?'danger':'success'?>'>
							<div class='panel-heading'>
								Precondition
							</div>
							<div class='panel-body'>
								<textarea name='precondition'><?&rule['precondition']?></textarea>
							</div>
							<div class='panel-footer'>
								<tt class='text-<?=error?'danger':'success'?>'><?&error?:''?></tt>
							</div>
						</div>
					</div>
					<?try{Eval.me('({'+rule['rendition']+'})');}catch(e){error=e;}?>
					<div class='col-lg-6'>
						<div class='panel panel-<?=error?'danger':'success'?>'>
							<div class='panel-heading'>
								Rendition
							</div>
							<div class='panel-body'>
								<textarea name='rendition'><?&rule['rendition']?></textarea>
							</div>
							<div class='panel-footer'>
								<tt class='text-<?=error?'danger':'success'?>'><?&error?:''?></tt>
							</div>
						</div>
					</div>
				</div>
				<div class=''>
					<?try{Eval.me('({'+rule['computation']+'})');}catch(e){error=e;}?>
					<div class='panel panel-<?=error?'danger':'success'?>'>
						<div class='panel-heading'>
							Computation
						</div>
						<div class='panel-body'>
							<textarea name='computation'><?&rule['computation']?></textarea>
						</div>
						<div class='panel-footer'>
							<tt class='text-<?=error?'danger':'success'?>'><?&error?:''?></tt>
						</div>
						</div>
				</div>
			</form>
		<?}?>
		<table class='table table-hover table-bordered table-condensed'>
			<thead>
				<tr>
					<th>Defined rules</th>
				</tr>
			</thead>
			<?heap.list().each{key->
				def rule=heap.get(key);?>
				<tr>
					<td>
						<a href='?rule=<?=key?>'>
							<span class='label label-info'><?&rule['protocol']?></span>
							<?&rule['label']?>
							<span class='text-muted'><?&rule['description']?:''?></span>
							<?if(rule['hot']){?>
								<span class='label label-danger'><i class='glyphicon glyphicon-fire'></i> Hot, baby!</span>
							<?}?>
						</a>
					</td>
				</tr>
			<?}?>
		</table>
	</body>
</html>