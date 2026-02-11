import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import PostCard from '../components/features/PostCard';

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

const mockPost = {
  description: 'Beautiful bird sighting',
  author: 'birder18',
  authorId: 'user123',
  authorProfilePic: '/images/profile_pictures/user123.jpg',
  dateTime: new Date('2025-01-23'),
  likes: 5,
  comments: 2,
  image: '/images/post1.jpg',
};

describe('PostCard Component', () => {
  test('should render post card with author name and description', () => {
    renderWithRouter(<PostCard {...mockPost} />);
    expect(screen.getByText('birder18')).toBeInTheDocument();
    expect(screen.getByText('Beautiful bird sighting')).toBeInTheDocument();
  });

  test('should display correct like and comment counts', () => {
    renderWithRouter(<PostCard {...mockPost} />);
    expect(screen.getByText('5')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });
});