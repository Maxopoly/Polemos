function orderSel(e) {
  if (e.value === 'AN01') {
    sortlist(0);
  } else if (e.value === 'AN02') {
    sortlist(1);
  } else if (e.value === 'AN03') {
    sortlist(2);
  } else if (e.value === 'AN04') {
    sortlist(3);
  } else if (e.value === 'AN05') {
    sortlist(4);
  } else if (e.value === 'AN06') {
    sortlist(5);
  }
}

function sortlist(offset) {
  var toSort = document.getElementById('infoHolder').children;
  toSort = Array.prototype.slice.call(toSort, 0);
  toSort.sort(function(a, b) {
    var aord = +a.id.split('-')[2 + offset];
    var bord = +b.id.split('-')[2 + offset];
    return aord - bord;
  });
  var parent = document.getElementById('infoHolder');
  parent.innerHTML = "";

  for(var i = 0, l = toSort.length; i < l; i++) {
    parent.appendChild(toSort[i]);
  }
}