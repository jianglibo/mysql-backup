/// <reference types="jest" />
import React from 'react';
import TestRenderer from 'react-test-renderer';
import ActionMenu from './action-menu';
import { ActionMenuDescription } from './action-menu-desc';

test('Link changes the class when hovered', () => {
  console.log(TestRenderer);
  const component = TestRenderer.create(
    <ActionMenu baseUrl="/app/server" selectedItems={[]} menuDescription={new ActionMenuDescription("edit")} />
  );
  let tree = component.toJSON();
  expect(tree).toMatchSnapshot();

  // manually trigger the callback
  tree.props.onMouseEnter();
  // re-rendering
  tree = component.toJSON();
  expect(tree).toMatchSnapshot();

  // manually trigger the callback
  tree.props.onMouseLeave();
  // re-rendering
  tree = component.toJSON();
  expect(tree).toMatchSnapshot();
});
