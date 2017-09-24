import { BrowserPage } from './app.po';

describe('browser App', () => {
  let page: BrowserPage;

  beforeEach(() => {
    page = new BrowserPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!');
  });
});
