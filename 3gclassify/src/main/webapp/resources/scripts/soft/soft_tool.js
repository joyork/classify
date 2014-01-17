//分页的跳转到
function jumpTo(pageNo, pageTotal, url) {
	var patrn=/^(([0-9]{1})|([1-9]{1}[0-9]{0,8}))$/;
	if (!patrn.test(pageNo)) 
		pageNo=1;
	if (pageNo > pageTotal) {
		pageNo=pageTotal;
	}
	if (pageNo < 1) {
		pageNo=1;
	}
	url=url.replace(/{page}/,pageNo);
	window.location.href = url;
}
//分页 每页显示的条数变化
function pageSizeChanged(pageSize,newSize,url)
{
	url=url.replace(/{page}/,1);
	url=url.replace("pageSize="+pageSize,"pageSize="+newSize);
	window.location.href = url;
}
// 软件权重值改变事件
function recommedChanged(id) {
	$("#recommendBtn" + id).removeAttr("disabled");
}

// end of 软件权重值改变事件

function changeBtn(id) {

	if ($("#recommendBtn" + id).text() == "修改") {
		$("#recommend" + id).css("display", "inline");
		$("#datarecommend" + id).css("display", "none");
		$("#recommendBtn" + id).html("保存");
	} else {
		changeRecommend(id);

	}

}
// 修改推荐权重值
function changeRecommend(id) {
	var recommend = $("#recommend" + id).val();
	var reg = /^[1-9][0-9]?$/;
	if (recommend != 0) {
		if (!reg.test(recommend)) {
			alert("权重值无效.");
			$("#recommend" + id).focus();
			return;
		}
	}
	$("#recommendBtn" + id).html("修改");
	jQuery.ajax({
		type : "POST",
		async : false,
		url : "/admin/ms/recommend",
		data : {
			softid : id,
			val : recommend
		},
		dataType : 'json',
		beforeSend : function() {
		},
		success : function(data) {
		}
	});// end ajax
	// $("#recommendBtn" + id).attr("disabled", "true");
	window.location.reload();
}
// end of修改推荐权重值

// 推荐软件(废弃)
function recommend(id) {
	$("#recommend_dialog").dialog({
		resizable : false,
		height : 200,
		width : 380,
		modal : true,
		buttons : {
			"确定" : function() {
				jQuery.ajax({
					type : "POST",
					async : false,
					url : "/admin/ms/recommend",
					data : {
						softid : id
					},
					dataType : 'json',
					beforeSend : function() {
					},
					success : function(data) {
					}
				});// end ajax
				$(this).dialog("close");
				window.location.reload();
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});// end dialog
}
// end of 推荐软件

// 取消推荐
function cancelRecommend(ids) {
	$("#cancel_recommend_dialog").dialog({
		resizable : false,
		height : 200,
		width : 380,
		modal : true,
		buttons : {
			"确定" : function() {
				jQuery.ajax({
					type : "POST",
					async : false,
					url : "/admin/ms/delete",
					data : {
						msids : ids,
						del : 3
					},
					dataType : 'json',
					beforeSend : function() {
					},
					success : function(data) {
					}
				});// end ajax
				$(this).dialog("close");
				window.location.reload();
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});// end dialog
}

// 取消推荐单个
function cancelRecommendSingle(id) {
	cancelRecommend(id);
}
// end of 取消推荐

$(document)
		.ready(
				function() {
					// 级联加载分类
					$("#platform_select")
							.change(
									function() {
										var platformid = $("#platform_select")
												.val();
										if (platformid == "") {
											$('#categoryid_select').find(
													'option').remove();
											var html = '<option value="">全部</option>';
											$('#categoryid_select')
													.append(html);
											return;
										}
										var targetUrl = "/admin/category/listjson/"
												+ platformid;
										var t = new Date().getTime();
										jQuery
												.ajax({
													async : false,
													type : 'POST',
													url : targetUrl,
													data : {
														"t" : t
													},
													success : function(jsondata) {
														categorys = eval(jsondata);
														$('#categoryid_select')
																.find('option')
																.remove();
														var html = '<option value="">全部</option>';
														jQuery
																.each(
																		categorys,
																		function(
																				entryIndex,
																				entry) {
																			var path = entry['path'];
																			var count = path.length / 10;
																			var tag = "";
																			for ( var i = 0; i < count; i++)
																				tag += "-";
																			if (tag == "-")
																				tag = "";
																			html += '<option value="'
																					+ entry['id']
																					+ '">'
																					+ tag
																					+ entry['name']
																					+ '</option>';

																		});// each
														$('#categoryid_select')
																.append(html);
													}
												});// ajax

									});
					// end of 级联加载分类

					$("#copy_platform_select")
							// 复制软件的级联加载
							.change(
									function() {
										var platformid = $(
												"#copy_platform_select").val();
										if (platformid == "") {
											$('#copy_categoryid_select').find(
													'option').remove();
											var html = '<option value="">全部</option>';
											$('#copy_categoryid_select')
													.append(html);
											return;
										}
										var targetUrl = "/admin/category/listjson/"
												+ platformid;
										var t = new Date().getTime();
										jQuery
												.ajax({
													async : false,
													type : 'POST',
													url : targetUrl,
													data : {
														"t" : t
													},
													success : function(jsondata) {
														categorys = eval(jsondata);
														$(
																'#copy_categoryid_select')
																.find('option')
																.remove();
														var html = '<option value="">全部</option>';
														jQuery
																.each(
																		categorys,
																		function(
																				entryIndex,
																				entry) {
																			var path = entry['path'];
																			var count = path.length / 10;
																			var tag = "";
																			for ( var i = 0; i < count; i++)
																				tag += "-";
																			if (tag == "-")
																				tag = "";
																			html += '<option value="'
																					+ entry['id']
																					+ '">'
																					+ tag
																					+ entry['name']
																					+ '</option>';

																		});// each
														$(
																'#copy_categoryid_select')
																.append(html);
													}
												});// ajax

									});

					//

					// 批量修改分类
					$('input[name="recategory_all"]')
							.click(
									function() {
										// 选择新分类
										var platformid = $(
												"#searchedPlatformId").val();
										if (platformid == "") {
											$("#recategory_alert")
													.dialog(
															{
																resizable : false,
																height : 200,
																width : 380,
																modal : true,
																buttons : {
																	"确定" : function() {
																		$(this)
																				.dialog(
																						"close");
																	}
																}
															});
											return;
										}

										var idarray = new Array();
										var ids = "";
										$('input[name="softs"]').each(
												function(i) {
													if (this.checked) {
														idarray.push($(this)
																.attr("id"));
													}
												});
										if (idarray.length == 0) {
											showmsg("你没有选中任何一项.");
											return;
										} else {
											ids = idarray.join(',');
										}

										var targetUrl = "/admin/category/listjson/"
												+ platformid;
										var t = new Date().getTime();
										jQuery
												.ajax({
													async : false,
													type : 'POST',
													url : targetUrl,
													data : {
														"t" : t
													},
													success : function(jsondata) {
														categorys = eval(jsondata);
														$('#category_new')
																.find('option')
																.remove();
														var html = '<option value="">请选择分类</option>';
														jQuery
																.each(
																		categorys,
																		function(
																				entryIndex,
																				entry) {
																			var path = entry['path'];
																			var count = path.length / 10;
																			var tag = "";
																			for ( var i = 0; i < count; i++)
																				tag += "-";
																			if (tag == "-")
																				tag = "";
																			html += '<option value="'
																					+ entry['id']
																					+ '">'
																					+ tag
																					+ entry['name']
																					+ '</option>';
																		});// each
														$('#category_new')
																.append(html);
													}
												});// ajax

										$("#selectCategory_dialog").dialog({
											resizable : false,
											height : 200,
											width : 380,
											modal : true,
											buttons : {
												"确定" : function() {
													$(this).dialog("close");
												},
												"取消" : function() {
													$(this).dialog("close");
												}
											}
										});

										$("#selectCategory_dialog")
												.bind(
														"dialogclose",
														function(event) {
															var newNodeId = $(
																	'#category_new')
																	.val();
															if (newNodeId == ""
																	|| newNodeId == undefined) {
																// alert("没有选择新分类.");
																return;
															} else {
																jQuery
																		.ajax({
																			type : "POST",
																			async : false,
																			url : "/admin/ms/recategory",
																			data : {
																				msids : ids,
																				del : 0,
																				categoryid : newNodeId
																			},
																			dataType : 'json',
																			beforeSend : function() {
																			},
																			success : function(
																					data) {
																			}
																		});// end
																// ajax
																window.location
																		.reload();

															}// else
														});
									});

					// end of 批量修改分类

					// 批量取消推荐
					$('input[name="cancel_recommend_all"]').click(function() {

						var idarray = new Array();
						var ids = "";
						$('input[name="softs"]').each(function(i) {
							if (this.checked) {
								idarray.push($(this).attr("id"));
							}
						});
						if (idarray.length == 0) {
							showmsg("你没有选中任何一项.");
							return;
						} else {
							ids = idarray.join(',');
							cancelRecommend(ids);
						}

					});
					// end of 批量取消推荐

				});// end of document ready
