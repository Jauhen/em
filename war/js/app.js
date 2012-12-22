window.Backbone = Backbone;

var AppRouterClass = Backbone.Router.extend({
  memes: new Memes(MEMES_JSON),
  memesListEl: $('#memesList'),
  comments: new Comments(),

  routes: {
    '': 'start',
    'popular': 'popular',
    'all': 'all',
    'top': 'top',
    'meme/:id': 'showOneMeme'
  },

  initialize: function() {
    $('#delete').hide();
    // Make header ('<epam:memegen>') a link to the home
    $('#header')
      .click(function() {
        Backbone.history.navigate('', true);
      });
  },

  start: function() {
    ga.trackPage();
    this.showAllMemes_();
  },

  all: function() {
    ga.trackPage('/all');
    this.memes.fetch({
      data: {filter: 'all'}, 
      success: _.bind(this.start, this)});
  },

  popular: function() {
    ga.trackPage('/popular');
    this.memes.fetch({
      data: {filter: 'popular'}, 
      success: _.bind(this.start, this)});
  },

  top: function() {
    ga.trackPage('/top');
    this.memes.fetch({
      data: {filter: 'top'}, 
      success: _.bind(this.start, this)});
  },

  getComments: function(memeId) {
    console.log("getComments:" + memeId);
    this.comments.memeId = memeId;
    this.comments.fetch({
      data: {id: memeId},
      success: _.bind(this.showComments, this)});
  },

  getMemes: function() {
    this.memes = new Memes();
    this.memes.fetch({success: $.proxy(this.onSuccessFetchAll_, this)});
  },

  onMemeAdded: function(meme) {
    this.memes.unshift(meme);
    var memeView = new MemeView({model: meme});
    this.memesListEl.prepend(memeView.render().$el);
  },

  showComments: function() {
    console.log("showComments");
    console.log(this.comments);

    this.memesListEl.append(new CommentsView({
      model: this.comments,
    }).render().$el);
  },

  showOneMeme: function(id) {
    ga.trackPage('/meme/' + id);
    var meme = this.memes.get(id);
    var memeView = new MemeView({model: meme, className: 'meme memeBig'});
    this.memesListEl.html(memeView.render(50).$el);
    this.memesListEl.append('<br/>');
    var button = $('#delete');
    button.on('click', $.proxy(function() {
      $('#delete').prop('disabled', true);
      Msg.info('Deleting...');
      meme.destroy({success: $.proxy(this.onSuccessDestroy_, this)})
    }, this));
    this.getComments(id);
  },

  showAllMemes_: function() {
    this.memesListEl.empty();
    for (var i = 0; i < this.memes.length; i++) {
      var memeView = new MemeView({model: this.memes.at(i)});
      this.memesListEl.append(memeView.render().$el);
    }
  },

  onSuccessDestroy_: function(model, resp) {
    Msg.info('Deleted!', 1500);
    this.memes.remove(model);
    Backbone.history.navigate('', true);
  }
});

$.ajaxSetup({
  'cache': false,
  // doesn't actually work, because of backbone bug https://github.com/documentcloud/backbone/issues/1875
  'contentType': 'application/json; charset=UTF-8'
});


var AppRouter = new AppRouterClass();
if (IS_AUTHENTICATED === false) {
  ga.trackNoAuth();
}

// Trigger the initial route and enable HTML5 History API support
Backbone.history.start();
