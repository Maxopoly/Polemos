var sorting = 0;;

function sortlist(offset) {
  sorting = +offset;
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

function showOnlyType(typeToShow) {
  var allEntries = document.getElementById('infoHolder').children;
  for(var i = 0; i < allEntries.length; i++) {
    var entry = allEntries [i];
    if(entry.id.split('-')[8].localeCompare(typeToShow) == 0) {
      entry.style.display = 'block';
    }
    else {
      entry.style.display = 'none';
    }
  }
}

function hideNonPlayerAvatar() {
  var allEntries = document.getElementById('infoHolder').children;
  for(var i = 0; i < allEntries.length; i++) {
    var entry = allEntries [i];
    var avatar = entry.children [0].children[0];
    if(entry.id.split('-')[8].localeCompare('player') != 0) {
      avatar.style.display = 'none';
    }
  }
}

window.onload=function() {showOnlyType('player');hideNonPlayerAvatar();};