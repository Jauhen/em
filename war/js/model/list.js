var memes = new Memes();
for (var i = 0; i < 10; i++) {
  memes.push(new Meme({
    url: 'https://memegen.googleplex.com/memeimage?k=11637038',
    messages: [
      {text: 'This Meme', layout: 'top-center'},
      {text: 'Is Deprecated', layout: 'bottom-center'}]
  }));

  memes.push(new Meme({
    url: 'https://memegen.googleplex.com/memeimage?k=11567160',
    messages: [
      {text: 'HolidayS are comming!', layout: 'bottom-center'}]
  }));

  memes.push(new Meme({
    url: 'https://memegen.googleplex.com/memeimage?k=11485229',
    messages: [
      {text: 'Monogomous', layout: 'bottom-center'}]
  }));
}

// memes.fetch();

for (var i = 0; i < 30; i++) {
  var memeView = new MemeView({model: memes.at(i)});

  $('#main_area').append(memeView.render().$el);
}
