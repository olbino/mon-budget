var CACHE = "mon-budget-v5";
var ASSETS = ["./","./index.html","./manifest.webmanifest","./icon-180.png","./icon-192.png","./icon-512.png"];

self.addEventListener("install", function(e){
  e.waitUntil(caches.open(CACHE).then(function(c){ return c.addAll(ASSETS); }));
  self.skipWaiting();
});

self.addEventListener("activate", function(e){
  e.waitUntil(caches.keys().then(function(ks){
    return Promise.all(ks.map(function(k){ if(k!==CACHE) return caches.delete(k); }));
  }));
  self.clients.claim();
});

self.addEventListener("fetch", function(e){
  if(e.request.method !== "GET") return;
  var req = e.request;
  var isHTML = req.mode === "navigate" || (req.headers.get("accept") || "").indexOf("text/html") !== -1;
  if(isHTML){
    // Réseau d'abord : toujours la dernière version de la page quand on est en ligne.
    e.respondWith(
      fetch(req).then(function(resp){
        var copy = resp.clone();
        caches.open(CACHE).then(function(c){ c.put("./index.html", copy); });
        return resp;
      }).catch(function(){ return caches.match("./index.html"); })
    );
  } else {
    // Ressources (icônes, manifeste) : cache d'abord.
    e.respondWith(caches.match(req).then(function(r){ return r || fetch(req); }));
  }
});
